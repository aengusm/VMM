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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.cycling74.max.*;

/*
 * Three inlets:
 * - The left inlet will accept lists corresponding to the history (which is 'reduced'), as well as various messages.
 * - The middle inlet will accept lists corresponding to reduction data.
 * - The right inlet will accept lists corresponding to raw data.
 *
 * When the system is learning:
 * - lists will be sent to two inlets in parallel (right and middle), one raw data point for every reduction data point.
 * - the 'learn' message in the left inlet will cause the data received since the last learn message, to be
 *   assimilated into the VMM tree
 * - the list of reduction data points will also form the 'history'
 * - the bang message will cause a single raw output to be generated using the current history and the VMM tree
 *
 * Max Patch Demos
 * - one will have to show how to do a straightforward Markov model
 */

public class VMM extends MaxObject {

	private ArrayList<ArrayList<Integer>> raw = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> reduction = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> history = new ArrayList<ArrayList<Integer>>();
	private VmmTree model;

	/**
	 * Constructor
	 * @param args - expects 1 integer setting the maximum order of the VMM.
	 */
	public VMM(Atom[] args) {
		declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL});
		createInfoOutlet(false);
		setInletAssist(new String[] {"Various Messages, History Data", "Reduction Data", "Raw Data"});
		setOutletAssist(new String[] {"Generated Output"});
		int model_order = 10;
		for(int i = 0; i < args.length; i++) {
			if(args[i].isInt())
				model_order = args[i].getInt();
		}
		this.model = new VmmTree(model_order);
	}

	/**
	 * Respond to the bang message.
	 * Bang in the left inlet generates output.
	 */
	public void bang() {
		int inlet_num = getInlet();
		if (inlet_num == 0) {
			this.generate_output(this.model.getOrder());
		}
	}

	/**
	 * Respond to the 'gen' message which should be followed by an int.
	 * This generates output using the int as the maximum order.
	 * @param gen_order
	 */
	public void gen(int gen_order) {
		int inlet_num = getInlet();
		if (inlet_num == 0) {
			if (gen_order < 0) {
				post("Cannot generate with order less than 0!");
			}
			else if (gen_order > this.model.getOrder()) {
				post("Cannot generate with order higher than model order (model order is " + this.model.getOrder() + ")");
			}
			else {
				this.generate_output(gen_order);
			}
		}
	}

	/**
	 * Respond to the genstart message by outputting a random sequence start.
	 */
	public void genstart() {
		int inlet_num = getInlet();
		if (inlet_num == 0) {
			if (this.model.getSize() > 0) {
				ArrayList<Integer> gen = this.model.gen_start();
				Atom[] out_atoms = new Atom[gen.size()];
				for (int i = 0; i < gen.size(); i++) {
					out_atoms[i] = Atom.newAtom(gen.get(i));
				}
				outlet(0, out_atoms);
			} else {
				post("Cannot generate: VMM is empty.");
			}
		}
	}

	/**
	 * Generate output with a given order (if greater than model order, model order will be used).
	 * @param gen_order
	 */
	private void generate_output(int gen_order) {
		ArrayList<Integer> gen = new ArrayList<Integer>();
		if (model.getSize() > 0) {
			gen = model.generate(this.history);
			Atom[] out_atoms = new Atom[gen.size()];
			for (int i = 0; i < gen.size(); i++) {
				out_atoms[i] = Atom.newAtom(gen.get(i));
			}
			outlet(0, out_atoms);
		} else {
			post("Cannot generate: VMM is empty.");
		}
	}

	/**
	 * Respond to an int in one of the inlets
	 */
	public void inlet(int i) {
		int inlet_num = getInlet();
		ArrayList<Integer> new_data = new ArrayList<Integer>();
		new_data.add(i);
		switch (inlet_num) {
		case 0:
			this.addHistory(new_data);
			break;
		case 1:
			this.addReduction(new_data);
			break;
		case 2:
			this.addRaw(new_data);
			break;
		default:
			break;
		}
	}

	/**
	 * Respond to a list in one of the inlets
	 */
	public void list(Atom[] args){
		int inlet_num = getInlet();
		ArrayList<Integer> new_data = new ArrayList<Integer>();
		Atom a;
		for(int i = 0; i < args.length; i++) {
			a = args[i];
			if(a.isFloat())
				error("VMM: lists must contain integers only. Received: " + a.getFloat());
			else if(a.isInt())
				new_data.add(a.getInt());
			else if(a.isString())
				error("VMM: lists must contain integers only. Received: " + a.getString());
		}
		switch (inlet_num) {
		case 0:
			this.addHistory(new_data);
			break;
		case 1:
			this.addReduction(new_data);
			break;
		case 2:
			this.addRaw(new_data);
			break;
		default:
			break;
		}
	}

	/**
	 * Add a data point to the raw data.
	 * @param new_raw
	 */
	private void addRaw(ArrayList<Integer> new_raw) {
		this.raw.add(new_raw);
	}

	/**
	 * Add a data point to the reduction data.
	 * @param new_reduction
	 */
	private void addReduction(ArrayList<Integer> new_reduction) {
		this.reduction.add(new_reduction);
	}

	/**
	 * Add a data point to the history data.
	 * @param new_history
	 */
	private void addHistory(ArrayList<Integer> new_history) {
		this.history.add(new_history);
	}

	/**
	 * Respond to 'learn' message in leftmost inlet.
	 * Have model learn from recent data.
	 */
	public void learn() {
		int inlet_num = getInlet();
		if (inlet_num == 0) {
			if (reduction.size() != raw.size()) {
				post("Cannot learn: reduction list and raw list have different lengths.");
			} else if (reduction.size() > 0 && raw.size() > 0) {
				this.model.learn(reduction, raw);
				this.reduction.clear();
				this.raw.clear();
			} else {
				post("Cannot learn: no input.");
			}
		}
	}

	/**
	 * Respond to the 'printtree' message.
	 * Print the VMM tree to the Max window.
	 */
	public void printmodel() {
		String[] model_strings = model.toString().split("\n");
		for (int i = 0; i < model_strings.length; i++) {
			post(model_strings[i]);
		}
	}

	/**
	 * Respond to the 'clearhistory' message.
	 * Clear the stored history.
	 */
	public void clearhistory() {
		this.history.clear();
	}

	/**
	 * Respond to the 'printhistory' message.
	 * Print the history to the Max window.
	 */
	public void printhistory() {
		if (this.history.size() == 0) {
			post("History is empty");
		} else {
			for (ArrayList<Integer> h : this.history) {
				post(h.toString());
			}
		}
	}

	/**
	 * Respond to the 'cleardata' message.
	 * Clear the previously input raw and reduction data.
	 */
	public void cleardata() {
		this.raw.clear();
		this.reduction.clear();
	}

	/**
	 * Respond to the 'clearall' message.
	 * Clear everything.
	 */
	public void clearall() {
		this.clearmodel();
		this.cleardata();
		this.clearhistory();
	}

	/**
	 * Respond to the 'clearmodel' message.
	 * Clear the VMM set up an empty one.
	 */
	public void clearmodel() {
		int model_order = this.model.getOrder();
		this.model = new VmmTree(model_order);
	}

	/**
	 * Respond to the 'save' message.
	 * Save the VMM to a file.
	 * @param filename
	 */
	public void save(Atom[] filename) {
		if (!filename[0].isString()) {
			post("Message 'save' must be followed by file name.");
			return;
		}
		String f = filename[0].getString();
		try {
			File file = new File(f);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
			outputStream.writeObject(this.model);
			outputStream.close();
		} catch (IOException e) {
			post("Save model failed with filename: " + f);
			post("Exception thrown:");
			post(e.getMessage());
		}
	}

	/**
	 * Respond to the 'load' message.
	 * Load the VMM from a file.
	 * @param filename
	 */
	public void load(Atom[] filename) {
		if (!filename[0].isString()) {
			post("Message 'load' must be followed by file name.");
			return;
		}
		int order = this.model.getOrder();
		String f = filename[0].getString();
		try {
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fis);
			VmmTree temp_model = (VmmTree)in.readObject();
			in.close();
			this.model = temp_model;
			post("Model loaded from " + f);
			this.printmodelsummary();
		} catch (IOException e) {
			this.model = new VmmTree(order);
			this.clearall();
			post("Load model failed with filename: " + f);
			post("Exception thrown:");
			post(e.getMessage());
			post("Current model has been reset");
		} catch (ClassNotFoundException e) {
			this.model = new VmmTree(order);
			this.clearall();
			post("Load model failed with filename: " + f);
			post("Exception thrown:");
			post(e.getMessage());
			post("Current model has been reset");
		}
	}

	/**
	 * Respond to the 'printmodelsummary' message.
	 */
	public void printmodelsummary() {
		post("VMM Order: " + this.model.getOrder());
		post("Number of Nodes: " + this.model.getSize());
		post("Number of possible outputs: " + this.model.getNumRawOuts());
	}
}
