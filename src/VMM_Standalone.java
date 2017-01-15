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

import java.util.ArrayList;


public class VMM_Standalone {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		VmmTree tree = new VmmTree(10);

		// Create and populate first training data sequence
		ArrayList<ArrayList<Integer>> raw_seq = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> red_seq = new ArrayList<ArrayList<Integer>>();

		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(0);
		a1.add(80);
		raw_seq.add(a1);
		ArrayList<Integer> a1r = new ArrayList<Integer>();
		a1r.add(0);
		red_seq.add(a1r);

		ArrayList<Integer> b1 = new ArrayList<Integer>();
		b1.add(1);
		b1.add(80);
		raw_seq.add(b1);
		ArrayList<Integer> b1r = new ArrayList<Integer>();
		b1r.add(1);
		red_seq.add(b1r);

		ArrayList<Integer> c1 = new ArrayList<Integer>();
		c1.add(2);
		c1.add(80);
		raw_seq.add(c1);
		ArrayList<Integer> c1r = new ArrayList<Integer>();
		c1r.add(2);
		red_seq.add(c1r);

		ArrayList<Integer> d1 = new ArrayList<Integer>();
		d1.add(3);
		d1.add(80);
		raw_seq.add(d1);
		ArrayList<Integer> d1r = new ArrayList<Integer>();
		d1r.add(3);
		red_seq.add(d1r);

		//
		System.out.println("Learning with red_seq length " + red_seq.size() + " and raw_seq length " + raw_seq.size());

		// Learn
		tree.learn(red_seq, raw_seq);
		System.out.println(tree.toString());

		// Create and populate first training data sequence
		raw_seq.clear();
		red_seq.clear();

		ArrayList<Integer> a2 = new ArrayList<Integer>();
		a2.add(0);
		a2.add(85);
		raw_seq.add(a2);
		ArrayList<Integer> a2r = new ArrayList<Integer>();
		a2r.add(0);
		red_seq.add(a2r);

		ArrayList<Integer> b2 = new ArrayList<Integer>();
		b2.add(1);
		b2.add(85);
		raw_seq.add(b2);
		ArrayList<Integer> b2r = new ArrayList<Integer>();
		b2r.add(1);
		red_seq.add(b2r);

		ArrayList<Integer> bb2 = new ArrayList<Integer>();
		bb2.add(1);
		bb2.add(89);
		raw_seq.add(bb2);
		ArrayList<Integer> bb2r = new ArrayList<Integer>();
		bb2r.add(1);
		red_seq.add(bb2r);

		ArrayList<Integer> c2 = new ArrayList<Integer>();
		c2.add(2);
		c2.add(85);
		raw_seq.add(c2);
		ArrayList<Integer> c2r = new ArrayList<Integer>();
		c2r.add(2);
		red_seq.add(c2r);

		tree.learn(red_seq, raw_seq);
		System.out.println(tree.toString());

		// Create a history
		ArrayList<ArrayList<Integer>> hist_seq = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> a1h = new ArrayList<Integer>();
		a1h.add(0);
		hist_seq.add(a1h);

		ArrayList<Integer> b1h = new ArrayList<Integer>();
		b1h.add(1);
		hist_seq.add(b1h);

		// ArrayList<Integer> c1h = new ArrayList<Integer>();
		// c1h.add(2);
		// hist_seq.add(c1h);

		// Generate an output
		ArrayList<Integer> g1 = tree.generate(hist_seq);
		System.out.println("Generated: " + g1.toString());

	}

}
