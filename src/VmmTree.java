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
import java.util.Random;

public class VmmTree extends Object implements Serializable
{
	Random randomGenerator = new Random();
	private static final long serialVersionUID = 1L;
	private ArrayList<VmmTreeNode> nodes = new ArrayList<VmmTreeNode>();
	private int order;
	private ArrayList<ArrayList<Integer>> raw_outs = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> seq_starts = new ArrayList<ArrayList<Integer>>();

	/**
	 * Constructor
	 * @param order Maximum order (sets max depth of tree)
	 */
	public VmmTree(int order)
	{
		this.order = order;
	}

	private void addNode(VmmTreeNode n)
	{
		nodes.add(n);
	}

	public int getSize()
	{
		return nodes.size();
	}

	private VmmTreeNode getNode(int index)
	{
		return this.nodes.get(index);
	}

	public int getOrder()
	{
		return order;
	}

	public int getNumRawOuts() {
		return this.raw_outs.size();
	}

	/*
	 * Draw a single Markov output, using the model's order.
	 */
	public ArrayList<Integer> generate(ArrayList<ArrayList<Integer>> history) {
		return this.generate(history, this.order);
	}

	/*
	 * Output a random sequence start.
	 * @return
	 */
	public ArrayList<Integer> gen_start() {
		int rand_choice = randomGenerator.nextInt(this.seq_starts.size());
		return this.seq_starts.get(rand_choice);
	}

	/*
	 * Draw a single Markov output, specifying the maximum order to use.
	 * If the specified maximum order is greater than the model order, it is silently reduced to the model order.
	 *
	 * (Note that this is not automatically added to the history- it can't be because the VmmTree does
	 * not know what the reduction function is.)
	 */
	public ArrayList<Integer> generate(ArrayList<ArrayList<Integer>> history, int gen_order) {

		if (gen_order > this.order) {
			gen_order = this.order;
		}

		int histlen = history.size();
		int num_continuations;
		int rand_choice;

		// If the order is 0 or there is no history, return a random continuation from the root of the VMM tree
		if (gen_order == 0 || histlen == 0) {
			num_continuations = this.getNode(0).getNumContinuations();
			rand_choice = randomGenerator.nextInt(num_continuations);
			return this.raw_outs.get(this.getNode(0).getContinuation(rand_choice));
		}

		// Otherwise descend down the VMM tree until limited by
		//   (i) lack of connections
		//  (ii) max order
		// (iii) history length
		int n = 1;
		boolean found = true;
		int node_id = 0; // initialise node_id to root node
		while (n <= gen_order && found && n <= histlen) {
			found = false;
			for (int link_id = 0; link_id < this.getNode(node_id).getNumLinks(); link_id++) {
				if (this.compareReductions(this.getNode(this.getNode(node_id).getLink(link_id)).getReduction(), history.get(histlen-n))) {
					n++;
					found = true;
					node_id = this.getNode(node_id).getLink(link_id);
					break;
				}
			}
		}

		// Randomly choose a continuation from node node_id and return
		num_continuations = this.getNode(node_id).getNumContinuations();
		rand_choice = randomGenerator.nextInt(num_continuations);
		return this.raw_outs.get(this.getNode(node_id).getContinuation(rand_choice));
	}

	/*
	 * Learn
	 */
	public void learn(ArrayList<ArrayList<Integer>> red_seq, ArrayList<ArrayList<Integer>> raw_seq)
	{
		if (this.getSize() == 0) {
			VmmTreeNode n = new VmmTreeNode();
			this.addNode(n);
		}

		int seqLen = red_seq.size();

		// Add first item in sequence to list of sequence starts
		this.seq_starts.add(raw_seq.get(0));

		for (int ssl = (seqLen-2); ssl >= 0; ssl--) // Iterate from the second last element to the first one
		{
			int nid = 0; // node id, initialised to root node

			// Add (ssl+1)th element of raw_seq to raw_outs and retain index (the continuation index)
			raw_outs.add(raw_seq.get(ssl+1));
			int cid = raw_outs.size() - 1;

			// Add continuation to the current node
			this.getNode(nid).addContinuation(cid);

			// Add the first note of the training sequence as a continuation from the root node
			if (ssl == 0){
				raw_outs.add(raw_seq.get(0));
				this.getNode(nid).addContinuation(raw_outs.size()-1);
			}

			int eid = ssl;
			int depth = 0;

			while (eid >= 0 && depth < this.order)
			{
				Boolean found = false;

				// Find next node
				for (int lid = 0; lid < this.getNode(nid).getNumLinks(); lid++)
				{
					if (this.compareReductions(this.getNode(this.getNode(nid).getLink(lid)).getReduction(), red_seq.get(eid)))
					{
						found = true;
						nid = this.getNode(nid).getLink(lid);
						this.getNode(nid).addContinuation(cid);
						break;
					}
				}

				// If node was not found, need to create a new one
				if (! found)
				{
					VmmTreeNode n = new VmmTreeNode();
					n.setReduction(red_seq.get(eid));
					n.addContinuation(cid);
					this.addNode(n);

					// add link from the current node to the newly created one
					int endNodeIdx = this.getSize()-1;
					this.getNode(nid).addLink(endNodeIdx);
					nid = endNodeIdx;
				}
				depth++;
				eid--;
			}
		}
	}

	/**
	 * Compare two reduction ArrayLists
	 * @param a
	 * @param b
	 * @return true if they are identical, false otherwise
	 */
	private boolean compareReductions(ArrayList<Integer> a, ArrayList<Integer> b) {
		if (a.size() != b.size()) return false;
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i) != b.get(i)) return false;
		}
		return true;
	}

	/**
	 * Print the VMM tree to output
	 */
	public String toString()
	{
		String s = new String();
		if (this.getSize() > 0) {
			// First, print out raw_outs
			s += "Raw (" + this.raw_outs.size() + " raw outputs):\n";
			for (int i=0; i < this.raw_outs.size(); i++) {
				s += (i + ": " + this.raw_outs.get(i).toString() + "\n");
			}
			s += "Tree (" + this.nodes.size() + " nodes):\n";
			VmmTreeNode n = this.getNode(0);
			s += n.toString() + "\n";
			String indent_string = "";
			for (int i = 0; i < n.getNumLinks(); i++) {
				s += get_tree_string(this.getNode(n.getLink(i)), indent_string);
			}
		}
		else {
			s = "Empty VMM tree";
		}
		return s;
	}

	/**
	 * Print a VMM tree node to output
	 * @param n
	 * @param indent_string
	 */
	private String get_tree_string(VmmTreeNode n, String indent_string)
	{
		String s = new String();
		indent_string = indent_string + "  ";
		s = indent_string + n.toString() + "\n";
		for( int i=0; i< n.getNumLinks(); i++ ){
			s += get_tree_string(this.getNode(n.getLink(i)), indent_string);
		}
		return s;
	}
}
