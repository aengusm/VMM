/*
    This file is part of VMM.

    VMM is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    VMM is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.Serializable;
import java.util.ArrayList;

public class VmmTreeNode extends Object implements Serializable
{
	/**
	 * Serial Version
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Integer> reduction = new ArrayList<Integer>();
	private ArrayList<Integer> links = new ArrayList<Integer>();
	private ArrayList<Integer> continuations = new ArrayList<Integer>();

	public void addLink(int link)
	{
		links.add(link);
	}

	public void addContinuation(int continuation)
	{
		continuations.add(continuation);
	}

	public void setReduction(ArrayList<Integer> reduction)
	{
		this.reduction = reduction;
	}

	public ArrayList<Integer> getReduction()
	{
		return reduction;
	}

	public int getNumLinks()
	{
		return links.size();
	}

	public int getLink(int index)
	{
		return links.get(index);
	}

	public int getNumContinuations()
	{
		return continuations.size();
	}

	public int getContinuation(int index)
	{
		return continuations.get(index);
	}

	public String toString() {
		String s = new String();
		s = "N, r=" + this.reduction.toString() + ", {";
		for (int i = 0; i < this.getNumContinuations(); i++) {
			s = s + this.getContinuation(i);
			if( i < this.getNumContinuations()-1) {
				s = s + " ";
			}
		}
		s = s + "}";
		return s;
	}
}
