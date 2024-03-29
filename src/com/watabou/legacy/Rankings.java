/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.opd.opdlib.OPDGame;
import com.watabou.legacy.actors.hero.HeroClass;
import com.watabou.legacy.utils.Utils;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.SystemTime;

public enum Rankings {
	
	INSTANCE;
	
	public static final int TABLE_SIZE	= 6;
	
	public static final String RANKINGS_FILE = "vanilla-rankings.dat";
	public static final String DETAILS_FILE = "vanilla-game_%d.dat";
	
	public ArrayList<Record> records;
	public int lastRecord;
	public int totalNumber;
	
	public void submit( boolean win ) {
		
		load();
		
		Record rec = new Record();
		
		rec.info	= Dungeon.resultDescription;
		rec.win		= win;
		rec.heroClass	= Dungeon.hero.heroClass;
		rec.armorTier	= Dungeon.hero.tier();
		rec.score	= score( win );
		
		String gameFile = Utils.format( DETAILS_FILE, SystemTime.now );
		try {
			Dungeon.saveGame( gameFile );
			rec.gameFile = gameFile;
		} catch (IOException e) {
			rec.gameFile = "";
		}
		
		records.add( rec );
		
		Collections.sort( records, scoreComparator );
		
		lastRecord = records.indexOf( rec );
		int size = records.size();
		if (size > TABLE_SIZE) {
			
			Record removedGame;
			if (lastRecord == size - 1) {
				removedGame = records.remove( size - 2 );
				lastRecord--;
			} else {
				removedGame = records.remove( size - 1 );
			}
			
			if (removedGame.gameFile.length() > 0) {
				OPDGame.deleteDatFile( removedGame.gameFile );
			}
		}
		
		totalNumber++;
		
		Badges.validateGamesPlayed();
		
		save();
	}
	
	private int score( boolean win ) {
		return (Statistics.goldCollected + Dungeon.hero.lvl * Dungeon.depth * 100) * (win ? 2 : 1);
	}
	
	private static final String RECORDS	= "records";
	private static final String LATEST	= "latest";
	private static final String TOTAL	= "total";
	
	public void save() {
		Bundle bundle = new Bundle();
		bundle.put( RECORDS, records );
		bundle.put( LATEST, lastRecord );
		bundle.put( TOTAL, totalNumber );
		
		try {
			OutputStream output = OPDGame.openDatOutput( RANKINGS_FILE, Game.MODE_PRIVATE );
			Bundle.write( bundle, output );
			output.close();
		} catch (Exception e) {
		}
	}
	
	public void load() {
		
		if (records != null) {
			return;
		}
		
		records = new ArrayList<Rankings.Record>();
		
		try {
			InputStream input = OPDGame.openDatInput( RANKINGS_FILE );
			Bundle bundle = Bundle.read( input );
			input.close();
			
			for (Bundlable record : bundle.getCollection( RECORDS )) {
				records.add( (Record)record );
			}			
			lastRecord = bundle.getInt( LATEST );
			
			totalNumber = bundle.getInt( TOTAL );
			if (totalNumber == 0) {
				totalNumber = records.size();
			}
			
		} catch (Exception e) {
		}
	}
	
	public static class Record implements Bundlable {
		
		private static final String REASON	= "reason";
		private static final String WIN		= "win";
		private static final String SCORE	= "score";
		private static final String TIER	= "tier";
		private static final String GAME	= "gameFile";
		
		public String info;
		public boolean win;
		
		public HeroClass heroClass;
		public int armorTier;
		
		public int score;
		
		public String gameFile;
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			
			info	= bundle.getString( REASON );
			win		= bundle.getBoolean( WIN );
			score	= bundle.getInt( SCORE );
			
			heroClass	= HeroClass.restoreInBundle( bundle );
			armorTier	= bundle.getInt( TIER );
			
			gameFile	= bundle.getString( GAME );
		}
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			
			bundle.put( REASON, info );
			bundle.put( WIN, win );
			bundle.put( SCORE, score );
			
			heroClass.storeInBundle( bundle );
			bundle.put( TIER, armorTier );
			
			bundle.put( GAME, gameFile );
		}
	}

	private static final Comparator<Record> scoreComparator = new Comparator<Rankings.Record>() {
		@Override
		public int compare( Record lhs, Record rhs ) {
			return (int)Math.signum( rhs.score - lhs.score );
		}
	};
}
