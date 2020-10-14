/*
 *
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dirsyncpro.sync;

/**
 * Represents a sync pair node in a sync pair tree
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class SyncPairNode{
	private SyncPair syncPair;
	private SyncPairNode parent;
	private SyncPairNode[] children;
	
	public SyncPairNode(SyncPair syncPair, SyncPairNode parent){
		this.syncPair = syncPair;
		this.parent = parent;
	}

	public SyncPair getSyncPair() {
		return syncPair;
	}

	public SyncPairNode getParent() {
		return parent;
	}

	public SyncPairNode[] getChildren() {
		return children;
	}
}
