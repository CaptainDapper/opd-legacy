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
package com.watabou.legacy.windows;

import com.watabou.legacy.actors.mobs.Mob;
import com.watabou.legacy.scenes.PixelScene;
import com.watabou.legacy.sprites.CharSprite;
import com.watabou.legacy.ui.BuffIndicator;
import com.watabou.legacy.utils.Utils;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class WndInfoMob extends WndTitledMessage {

	public WndInfoMob( Mob mob ) {
		
		super( new MobTitle( mob ), desc( mob ) );
		
	}
	
	private static String desc( Mob mob ) {
		
		StringBuilder builder = new StringBuilder( mob.description() );
		
		builder.append( "\n\n" + mob.state.status() + "." );
		
		return builder.toString();
	}
	
	private static class MobTitle extends Component {
		
		private static final int COLOR_BG	= 0xFFCC0000;
		private static final int COLOR_LVL	= 0xFF00EE00;
		
		private static final int BAR_HEIGHT	= 2;
		private static final int GAP	= 2;
		
		private CharSprite image;
		private BitmapText name;
		private ColorBlock hpBg;
		private ColorBlock hpLvl;
		private BuffIndicator buffs;
		
		private float hp;
		
		public MobTitle( Mob mob ) {
			
			hp = (float)mob.HP / mob.HT;
			
			name = PixelScene.createText( Utils.capitalize( mob.name ), 9 );
			name.hardlight( TITLE_COLOR );
			name.measure();	
			add( name );
			
			image = mob.sprite();
			add( image );
			
			hpBg = new ColorBlock( 1, 1, COLOR_BG );
			add( hpBg );
			
			hpLvl = new ColorBlock( 1, 1, COLOR_LVL );
			add( hpLvl );
			
			buffs = new BuffIndicator( mob );
			add( buffs );
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + GAP + BAR_HEIGHT - image.height );
			
			name.x = image.width + GAP;
			name.y = image.height - BAR_HEIGHT - GAP - name.baseLine();
			
			float w = width - image.width - GAP;
			
			hpBg.size( w, BAR_HEIGHT );
			hpLvl.size( w * hp, BAR_HEIGHT );
			
			hpBg.x = hpLvl.x = image.width + GAP;
			hpBg.y = hpLvl.y = image.height - BAR_HEIGHT;
			
			buffs.setPos( 
				name.x + name.width() + GAP, 
				name.y + name.baseLine() - BuffIndicator.SIZE );
			
			height = hpBg.y + hpBg.height();
		}
	}
}
