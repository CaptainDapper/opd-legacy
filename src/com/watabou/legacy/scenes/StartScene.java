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
package com.watabou.legacy.scenes;

import java.util.HashMap;

import com.watabou.legacy.Assets;
import com.watabou.legacy.Badges;
import com.watabou.legacy.Dungeon;
import com.watabou.legacy.GamesInProgress;
import com.watabou.legacy.PixelDungeon;
import com.watabou.legacy.actors.hero.HeroClass;
import com.watabou.legacy.effects.BannerSprites;
import com.watabou.legacy.effects.Speck;
import com.watabou.legacy.effects.BannerSprites.Type;
import com.watabou.legacy.scenes.InterlevelScene;
import com.watabou.legacy.scenes.IntroScene;
import com.watabou.legacy.scenes.PixelScene;
import com.watabou.legacy.scenes.StartScene;
import com.watabou.legacy.scenes.TitleScene;
import com.watabou.legacy.ui.Archs;
import com.watabou.legacy.ui.ExitButton;
import com.watabou.legacy.ui.Icons;
import com.watabou.legacy.ui.RedButton;
import com.watabou.legacy.utils.Utils;
import com.watabou.legacy.windows.WndChallenges;
import com.watabou.legacy.windows.WndClass;
import com.watabou.legacy.windows.WndMessage;
import com.watabou.legacy.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;

public class StartScene extends PixelScene {

	private static final float BUTTON_HEIGHT	= 24;
	private static final float GAP				= 2;
	
	private static final String TXT_LOAD	= "Load Game";
	private static final String TXT_NEW		= "New Game";
	
	private static final String TXT_ERASE		= "Erase current game";
	private static final String TXT_DPTH_LVL	= "Depth: %d, level: %d";
	
	private static final String TXT_REALLY	= "Do you really want to start new game?";
	private static final String TXT_WARNING	= "Your current game progress will be erased.";
	private static final String TXT_YES		= "Yes, start new game";
	private static final String TXT_NO		= "No, return to main menu";
	
	private static final String TXT_UNLOCK	= "To unlock this character class, slay the 3rd boss with any other class";
	
	private static final String TXT_WIN_THE_GAME = 
		"To unlock \"Challenges\", win the game with any character class.";
	
	private static final float WIDTH = 116;
	private static final float HEIGHT = 220;
	
	private static HashMap<HeroClass, ClassShield> shields = new HashMap<HeroClass, ClassShield>();
	
	private GameButton btnLoad;
	private GameButton btnNewGame;
	
	private boolean huntressUnlocked;
	private Group unlock;
	
	public static HeroClass curClass;
	
	@Override
	public void create() {
		
		super.create();
		
		Badges.loadGlobal();
		
		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;

		float left = (w - WIDTH) / 2;
		float top = (h - HEIGHT) / 2; 
		float bottom = h - top;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs ); 
		
		Image title = BannerSprites.get( Type.SELECT_YOUR_HERO );
		title.x = align( (w - title.width()) / 2 );
		title.y = top;
		add( title );
		
		btnNewGame = new GameButton( TXT_NEW ) {
			@Override
			protected void onClick() {
				if (GamesInProgress.check( curClass ) != null) {
					StartScene.this.add( new WndOptions( TXT_REALLY, TXT_WARNING, TXT_YES, TXT_NO ) {
						@Override
						protected void onSelect( int index ) {
							if (index == 0) {
								startNewGame();
							}
						}
					} );
					
				} else {
					startNewGame();
				}
			}
		};
		add( btnNewGame );

		btnLoad = new GameButton( TXT_LOAD ) {	
			@Override
			protected void onClick() {
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				Game.switchScene( InterlevelScene.class );
			}
		};
		add( btnLoad );	
		
		HeroClass[] classes = {
			HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE, HeroClass.HUNTRESS	
		};
		float shieldW = WIDTH / 2;
		float shieldH = Math.min( (bottom - BUTTON_HEIGHT - title.y - title.height()) / 2, shieldW * 1.2f );
		top = (bottom - BUTTON_HEIGHT + title.y + title.height() - shieldH * 2) / 2;
		for (int i=0; i < classes.length; i++) {
			ClassShield shield = new ClassShield( classes[i] );
			shield.setRect( 
				left + (i % 2) * shieldW, 
				top + (i / 2) * shieldH, 
				shieldW, shieldH );
			add( shield );
			
			shields.put( classes[i], shield );
		}
		
		unlock = new Group();
		add( unlock );
		
		ChallengeButton challenge = new ChallengeButton();
		challenge.setPos( 
			w / 2 - challenge.width() / 2,
			top + shieldH - challenge.height() / 2 );
		add( challenge );
		
		if (!(huntressUnlocked = Badges.isUnlocked( Badges.Badge.BOSS_SLAIN_3 ))) {
		
			BitmapTextMultiline text = PixelScene.createMultiline( TXT_UNLOCK, 9 );
			text.maxWidth = (int)WIDTH;
			text.measure();
			
			float pos = (bottom - BUTTON_HEIGHT) + (BUTTON_HEIGHT - text.height()) / 2;
			for (BitmapText line : text.new LineSplitter().split()) {
				line.measure();
				line.hardlight( 0xFFFF00 );
				line.x = PixelScene.align( left + WIDTH / 2 - line.width() / 2 );
				line.y = PixelScene.align( pos );
				unlock.add( line );
				
				pos += line.height(); 
			}
		}
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );
		
		curClass = null;
		updateClass( HeroClass.values()[PixelDungeon.lastClass()] );
		
		fadeIn();
	}
	
	private void updateClass( HeroClass cl ) {
		
		if (curClass == cl) {
			add( new WndClass( cl ) );
			return;
		}
		
		if (curClass != null) {
			shields.get( curClass ).highlight( false );
		}
		shields.get( curClass = cl ).highlight( true );
		
		if (cl != HeroClass.HUNTRESS || huntressUnlocked) {
		
			unlock.visible = false;
			 
			float buttonPos = (Camera.main.height + HEIGHT) / 2 - BUTTON_HEIGHT;
			
			float left = (Camera.main.width - WIDTH) / 2;
			
			GamesInProgress.Info info = GamesInProgress.check( curClass );
			if (info != null) {
				
				btnLoad.visible = true;
				btnLoad.secondary( Utils.format( TXT_DPTH_LVL, info.depth, info.level ) );
				btnNewGame.visible = true;
				btnNewGame.secondary( TXT_ERASE );
				
				float w = (WIDTH - GAP) / 2;
				
				btnLoad.setRect(
					left, buttonPos, w, BUTTON_HEIGHT );
				btnNewGame.setRect(
					btnLoad.right() + GAP, buttonPos, w, BUTTON_HEIGHT );
				
			} else {
				btnLoad.visible = false;
				
				btnNewGame.visible = true;
				btnNewGame.secondary( null );
				btnNewGame.setRect( left, buttonPos, WIDTH, BUTTON_HEIGHT );
			}
			
		} else {
			
			unlock.visible = true;
			btnLoad.visible = false;
			btnNewGame.visible = false;
			
		}
	}
	
	private void startNewGame() {

		Dungeon.hero = null;
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		
		if (PixelDungeon.intro()) {
			PixelDungeon.intro( false );
			Game.switchScene( IntroScene.class );
		} else {
			Game.switchScene( InterlevelScene.class );
		}	
	}
	
	@Override
	protected void onBackPressed() {
		PixelDungeon.switchNoFade( TitleScene.class );
	}
	
	private static class GameButton extends RedButton {
		
		private static final int SECONDARY_COLOR	= 0xCACFC2;
		
		private BitmapText secondary;
		
		public GameButton( String primary ) {
			super( primary );
			
			this.secondary.text( null );
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			secondary = createText( 6 );
			secondary.hardlight( SECONDARY_COLOR );
			add( secondary );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			if (secondary.text().length() > 0) {
				text.y = align( y + (height - text.height() - secondary.baseLine()) / 2 );
				
				secondary.x = align( x + (width - secondary.width()) / 2 );
				secondary.y = align( text.y + text.height() ); 
			} else {
				text.y = align( y + (height - text.baseLine()) / 2 );
			}
		}
		
		public void secondary( String text ) {
			secondary.text( text );
			secondary.measure();
		}
	}
	
	private class ClassShield extends Button {
		
		private static final float MIN_BRIGHTNESS	= 0.6f;
		
		private static final int WIDTH	= 24;
		private static final int HEIGHT	= 28;
		private static final int SCALE	= 2;
		
		private HeroClass cl;
		
		private Image avatar;
		private BitmapText name;
		private Emitter emitter;
		
		private float brightness;
		
		public ClassShield( HeroClass cl ) {
			super();
		
			this.cl = cl;
			
			avatar.frame( cl.ordinal() * WIDTH, 0, WIDTH, HEIGHT );
			avatar.scale.set( SCALE );
			
			name.text( cl.name() );
			name.measure();
			
			brightness = MIN_BRIGHTNESS;
			updateBrightness();
		}
		
		@Override
		protected void createChildren() {
			
			super.createChildren();
			
			avatar = new Image( Assets.AVATARS );
			add( avatar );
			
			name = PixelScene.createText( 9 );
			add( name );
			
			emitter = new Emitter();
			add( emitter );
		}
		
		@Override
		protected void layout() {
			
			super.layout();
			
			avatar.x = align( x + (width - avatar.width()) / 2 );
			avatar.y = align( y + (height - avatar.height() - name.height()) / 2 );
			
			name.x = align( x + (width - name.width()) / 2 );
			name.y = avatar.y + avatar.height() + SCALE;
			
			emitter.pos( avatar.x, avatar.y, avatar.width(), avatar.height() );
		}
		
		@Override
		protected void onTouchDown() {
			
			emitter.revive();
			emitter.start( Speck.factory( Speck.LIGHT ), 0.05f, 7 );
			
			Sample.INSTANCE.play( Assets.SND_CLICK, 1, 1, 1.2f );
			updateClass( cl );
		}
		
		@Override
		public void update() {
			super.update();
			
			if (brightness < 1.0f && brightness > MIN_BRIGHTNESS) {
				if ((brightness -= Game.elapsed) <= MIN_BRIGHTNESS) {
					brightness = MIN_BRIGHTNESS;
				}
				updateBrightness();
			}
		}
		
		public void highlight( boolean value ) {
			if (value) {
				brightness = 1.0f;
				name.hardlight( 0xCACFC2 );
			} else {
				brightness = 0.999f;
				name.hardlight( 0x444444 );
			}

			updateBrightness();
		}
		
		private void updateBrightness() {
			avatar.gm = avatar.bm = avatar.rm = avatar.am = brightness;
		}
	}
	
	private class ChallengeButton extends Button {
		
		private Image image;
		
		public ChallengeButton() {
			super();
			
			width = image.width;
			height = image.height;
			
			image.am = Badges.isUnlocked( Badges.Badge.VICTORY ) ? 1.0f : 0.5f;
		}
		
		@Override
		protected void createChildren() {
			
			super.createChildren();
			
			image = Icons.get( PixelDungeon.challenges() > 0 ? Icons.CHALLENGE_ON :Icons.CHALLENGE_OFF );
			add( image );
		}
		
		@Override
		protected void layout() {
			
			super.layout();
			
			image.x = align( x );
			image.y = align( y  );
		}
		
		@Override
		protected void onClick() {
			if (Badges.isUnlocked( Badges.Badge.VICTORY )) {
				add( new WndChallenges( PixelDungeon.challenges(), true ) {
					public void onBackPressed() {
						super.onBackPressed();
						image.copy( Icons.get( PixelDungeon.challenges() > 0 ? 
							Icons.CHALLENGE_ON :Icons.CHALLENGE_OFF ) );
					};
				} );
			} else {
				add( new WndMessage( TXT_WIN_THE_GAME ) );
			}
		}
		
		@Override
		protected void onTouchDown() {
			Sample.INSTANCE.play( Assets.SND_CLICK );
		}
	}
}
