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

import com.watabou.legacy.actors.mobs.npcs.NPC;
import com.watabou.legacy.utils.Utils;

public class WndQuest extends WndTitledMessage {
	
	public WndQuest( NPC questgiver, String text ) {
		super( questgiver.sprite(), Utils.capitalize( questgiver.name ), text );
	}
}
