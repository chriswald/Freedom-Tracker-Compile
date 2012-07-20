/**
 *     _______    __    __   ________   __________    _______
 *    /\   __ \  /\ \  /\ \ /\  ____ \ /\____  ___\  /\  ____\
 *    \ \ \_/\_\ \ \ \_\_\ \\ \ \___\ \\/___/\ \__/  \ \ \___/
 *     \ \ \\/_/  \ \  ____ \\ \  ___ <     \ \ \     \ \____`\
 *      \ \ \   __ \ \ \__/\ \\ \ \ /\ \     \ \ \     \/___/\ \
 *       \ \ \__\ \ \ \ \ \ \ \\ \ \\ \ \    _\_\ \____   __\_\ \
 *        \ \______\ \ \_\ \ \_\\ \_\\ \_\  /\_________\ /\_____ \
 *         \/______/  \/_/  \/_/ \/_/ \/_/  \/_________/ \/______/
 *                __      __    ________    __        ______
 *               /\ \    /\ \  /\  ____ \  /\ \      /\  ___`,
 *               \ \ \   \ \ \ \ \ \__/\ \ \ \ \     \ \ \_/\ \
 *                \ \ \   \ \ \ \ \ \_\_\ \ \ \ \     \ \ \\ \ \
 *                 \ \ \  _\ \ \ \ \  ____ \ \ \ \     \ \ \\ \ \
 *                  \ \ \_\ \_\ \ \ \ \__/\ \ \ \ \_____\ \ \\_\ \
 *                   \ \_________\ \ \_\ \ \_\ \ \______\\ \_____/
 *                    \/_________/  \/_/  \/_/  \/______/ \/____/
 *
 *         ->Freedom Tracker (Compiler)
 *         ->Developed By Christopher J. Wald
 *         ->Copyright 2012 (c) All Rights Reserved
 * 
 * 
 * @author  	Chris Wald
 * @date    	Mar 03, 2012 1:03 PM
 * @project 	Freedom Tracker Compile
 * @file    	FreedomCompiler.java
 * @description Freedom Tracker (Compiler) is a program used to build individual
 * 				weekly schedules showing the times students are in class and then
 * 				compile those individual schedules into one export file for use
 * 				with the companion program "Freedom Tracker". This program
 * 				provides tools to create new schedules (including creating a new
 * 				file for a student and adding classes to that schedule), remove
 * 				classes from a student's schedule, export all student schedule
 * 				files to one export file, steamline new schedule files into an
 * 				existing export file, import schedule files from an export file,
 * 				provide a listing of all schedule files, show the schedule of
 * 				selected users, and to merge two export files together. This is
 * 				a companion program to "Freedom Tracker" and exists for the
 * 				sole purpose of generating an export file for that program to
 * 				read. Everything else is really just gravy.
 * 
 * @license
 *
 * 	Redistribution and use in source and binary forms, with or without
 * 	modification, are permitted provided that the following conditions
 * 	are met:
 *
 *	- Redistributions of source code must retain the above copyright
 *	  notice, this list of conditions and the following disclaimer.
 *
 *	- Redistributions in binary form must reproduce the above copyright
 *	  notice, this list of conditions and the following disclaimer in the
 *	  documentation and/or other materials provided with the distribution.
 *
 *	- The name of Christopher J. Wald may not be used to endorse or promote
 *	  products derived from this software without specific prior written
 *	  permission.
 *
 * 	THIS SOFTWARE IS PRIVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * 	EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * 	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE,
 * 	ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * 	DIRECT, INDIRECT, INCIDENTAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING
 * 	BUT NOT LIMITED TO UNDESIRED ACTION, LOSS OF SECURITY, LOSS OF DATA, LOSS OF
 * 	SLEEP,  LOSS OF HAIR, OR EXPLOSIONS). USE AT YOUR OWN RISK.
 */

package home;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import pkg.BusyTime;
import pkg.Day;
import pkg.Schedule;
import pkg.Time;

public class FreedomCompiler {

	//              //
	//  VARIABLES   //
	//              //

	private String[] args;
	private int args_index = 0;

	private Schedule schedule = null;
	private Vector<Day> days = new Vector<Day>();
	private Time time = new Time();

	private String filename;

	private boolean edited = false;
	private boolean add = true;

	private enum State {HELP, ADDUSER, ADDCLASS, SETDAY, SETTIME, REMOVECLASS, SHOW, EXPORT, ADDEXPORT, IMPORT, LIST, MERGE, JTOP, PTOJ, ERR};
	private State state = State.ERR;


	//                      //
	//	PRIMARY FUNCTIONS   //
	//                      //

	// Analogous to public static void main(String[] args)
	public void run(String[] args) {
		this.args = args;

		if (this.args.length < 1)
			this.printHelp();

		// Primary Switch
		while (this.args_index < args.length) {
			this.readArgs();

			switch (this.state) {
				case HELP:
					this.printHelp();
					return;
				case ADDUSER:
					this.addUser();
					return;
				case ADDCLASS:
				case REMOVECLASS:
					this.editClass();
					break;
				case SETDAY:
					this.setDay();
					break;
				case SETTIME:
					this.setTime();
					break;
				case SHOW:
					this.show();
					return;
				case EXPORT:
					this.export();
					return;
				case ADDEXPORT:
					this.addExport();
					return;
				case IMPORT:
					this.importx();
					return;
				case LIST:
					this.list();
					return;
				case MERGE:
					this.merge();
					return;
				case JTOP:
					this.jtop();
					return;
				case PTOJ:
					this.ptoj();
					return;
				case ERR:
					this.printHelp();
					return;
				default:
					System.out.println("E: Undefined");
					this.printHelp();
					return;
			}
		}

		// Write all changes to selected user's schedule
		this.writeSchedule();
	}

	// Prints a list of accepted commands, followed by argument tokens
	private void printHelp() {
		System.out.println("\nFreedom Tracker Compiler - Christopher J. Wald - ~Destructor() Labs - 2012\n");
		System.out.println("freecomp.jar [-u | -c | -r | -e | -x | -i | -s | -l | -m | -h]\n");
		System.out.println("\t-u, --adduser [user]\tCreates a new user with empty schedule\n");
		System.out.println("\t-c, --addclass [user] -d [m|t|w|r|f] -t [start-end]\n\t\t\t\tAdds a new class to <user>'s schedule on days listed at time\n");
		System.out.println("\t-r, --removeclass [user] -d [m|t|w|r|f] -t [start-end]\n\t\t\t\tRemoves the specified class from <user>'s schedule\n");
		System.out.println("\t-e, --export [file]\tExports all schedules to one <file>.ccl for use with Freedom Tracker\n");
		System.out.println("\t-x, --addexport [file] [user...]\n\t\t\t\tAdds all listed <user>.cal's to <file>.ccl\n");
		System.out.println("\t-i, --import [file]\tReads in <file>.ccl and saves individual *.cal's for each user found, overwriting existing\n");
		System.out.println("\t-s, --show [user]\tDisplays the schedule for <user>\n");
		System.out.println("\t-l, --list\t\tLists all users\n");
		System.out.println("\t-m, --merge [save] [read...]\n\t\t\t\tMerges the <read>.ccl files and saves them as one <save>.ccl\n");
		//System.out.println("\t-b, --batchexport [file]\n\t\t\t\txxReads in all .sch files in the directory and exports as <file>.ccl\n");
		System.out.println("\t-h, --help\t\tDisplays this message\n");
		System.out.println("Ex:");
		System.out.println("\tfreecomp.jar -u chris");
		System.out.println("\tfreecomp.jar -c chris -d mwf -t 9:00-9:52");
		System.out.println("\tfreecomp.jar -e export");
	}

	// Create an empty schedule for a new user
	private void addUser() {
		if (this.args.length < 2) {
			System.out.println("E: Insufficient arguments");
			return;
		}
		else if (this.args.length > 3) {
			System.out.println("W: Ignoring Extra Arguments");
		}

		ObjectOutputStream fo;
		this.filename = this.args[this.args_index];

		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.filename + ".cal")));
			Schedule s = new Schedule();
			s.name = this.filename;
			fo.writeObject(s);
			fo.close();
			this.add = true;
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Read in the user's current schedule for editing and flags the created class for addition or deletion
	private void editClass() {
		if (this.args.length < 6) {
			System.out.println("E: Insufficient arguments");
			return;
		}

		ObjectInputStream fi;
		this.filename = this.args[this.args_index ++];

		try {
			fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.filename + ".cal")));
			this.schedule = (Schedule) fi.readObject();
			if (this.state == State.ADDCLASS)
				this.add = true;
			else
				this.add = false;
			fi.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Set the days for the new class
	private void setDay() {
		if (this.args.length < 6) {
			System.out.println("E: Insufficient Arguments");
			return;
		}

		String days = this.args[this.args_index ++];

		this.setDayInternal(days);
		this.edited = true;
	}



	// Set the time for the new classes
	private void setTime() {
		if (this.args.length < 6) {
			System.out.println("E: Insufficient Arguments");
			return;
		}

		String times = this.args[this.args_index++];

		this.setTimeInternal(times);

		this.edited = true;
	}

	// Writes out the changes to the user's schedule
	private void writeSchedule() {
		if (this.add)
			this.classToAdd();
		else
			this.classToRemove();
	}

	private void classToAdd() {
		// If nothing was changed don't change the file
		if (!this.edited)
			return;

		// Make new BusyTime's for the details entered
		Vector<BusyTime> busytime = this.buildBusyTimes();

		// Checks for duplicate events
		for (int i = 0; i < this.schedule.classes.size(); i ++) {
			int index = 0;
			while (index < busytime.size()) {
				if (busytime.get(index).equals(this.schedule.classes.get(i))) {
					System.out.println("W: Not adding duplicate class \"" + busytime.get(index) + "\"");
					busytime.remove(index);
				} else {
					index ++;
				}
			}
		}

		// Writes changes to buffer
		for (int i = 0; i < busytime.size(); i ++) {
			this.schedule.classes.add(busytime.get(i));
		}

		this.schedule.sort();

		// Writes buffer to file
		ObjectOutputStream fo;

		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.filename + ".cal")));
			fo.writeObject(this.schedule);
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Removes a class from a user's schedule
	private void classToRemove() {
		boolean removed = false;
		Vector<BusyTime> busytime = this.buildBusyTimes();

		for (int i = busytime.size() - 1; i >= 0; i --) {
			for (int j = this.schedule.classes.size() - 1; j >= 0; j --) {
				if (busytime.get(i).equals(this.schedule.classes.get(j))) {
					System.out.println("R: " + busytime.get(i).toString());
					this.schedule.classes.remove(j);
					removed = true;
				}
			}
		}

		if (!removed) {
			System.out.println("W: None removed");
		}

		// Writes buffer to file
		ObjectOutputStream fo;

		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.filename + ".cal")));
			fo.writeObject(this.schedule);
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Shows a user's schedule
	private void show() {
		if (this.args.length < 2) {
			System.out.println("E: Insufficient arguments");
			return;
		}

		Vector<String> filenames = new Vector<String>();
		while (this.args_index < this.args.length) {
			filenames.add(this.args[this.args_index++]);
		}

		for (String fn : filenames) {
			// Read in the current schedule
			ObjectInputStream fi;

			try {
				fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fn)));
				this.schedule = (Schedule) fi.readObject();
				fi.close();
			} catch (Exception e) {
				try {
					fn += ".cal";
					fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fn)));
					this.schedule = (Schedule) fi.readObject();
					fi.close();
				} catch (Exception ee) {
					System.out.println("W: Could not find file. Skipping \"" + fn + "\"");
					this.schedule = null;
				}
			}

			if (this.schedule != null) {

				this.schedule.sort();

				// Sort by day (but not time!)
				Vector<BusyTime> mon = new Vector<BusyTime>(0);
				Vector<BusyTime> tue = new Vector<BusyTime>(0);
				Vector<BusyTime> wed = new Vector<BusyTime>(0);
				Vector<BusyTime> thu = new Vector<BusyTime>(0);
				Vector<BusyTime> fri = new Vector<BusyTime>(0);

				for (int i = 0; i < this.schedule.classes.size(); i ++) {
					Day day = this.schedule.classes.get(i).day;
					switch (day) {
						case Monday:
							mon.add(this.schedule.classes.get(i));
							break;
						case Tuesday:
							tue.add(this.schedule.classes.get(i));
							break;
						case Wednesday:
							wed.add(this.schedule.classes.get(i));
							break;
						case Thursday:
							thu.add(this.schedule.classes.get(i));
							break;
						case Friday:
							fri.add(this.schedule.classes.get(i));
							break;
					}
				}

				String pre = "";

				if (this.args.length > 2) {
					pre = "    ";
					System.out.println(this.schedule.name);
				}

				this.showHelper(pre + "Mon", mon);
				this.showHelper(pre + "Tue", tue);
				this.showHelper(pre + "Wed", wed);
				this.showHelper(pre + "Thu", thu);
				this.showHelper(pre + "Fri", fri);
			}
		}
	}

	// Export all user schedules to a single .ccl file
	private void export() {
		if (this.args.length < 2) {
			System.out.println("E: Insufficient arguments");
			return;
		}

		Vector<Schedule> schedules = new Vector<Schedule>();
		Vector<String> files = this.findFilesWithExtension(".cal");

		// Read in schedules to buffer
		for (String s : files) {
			ObjectInputStream fi;
			try {
				fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(s + ".cal")));
				schedules.add((Schedule) fi.readObject());
				fi.close();
			} catch (Exception e) {
				System.out.println("W: Could not find file. Skipping \"" + s + "\"");
			}
		}

		if (schedules.size() == 0) {
			System.out.println("E: No files found. Nothing to do.");
			return;
		}

		this.writeSchedulesExport(this.args[this.args_index], schedules);
	}

	// Shows listing of all found user files (all *.cal files)
	private void list() {
		if (this.args.length > 1) {
			System.out.println("W: Ignoring extra arguments");
		}

		Vector<String> files = this.findFilesWithExtension(".cal");

		int index = 1;
		for (String s : files) {
			System.out.println((index ++) + ": " + s);
		}
	}

	// Adds a list of users to the exported *.ccl file
	@SuppressWarnings("unchecked")
	private void addExport() {
		if (this.args.length < 3) {
			System.out.println("E: Insufficient arguments");
			return;
		}

		String file = this.args[this.args_index ++] + ".ccl";

		Vector<Schedule> schedules = new Vector<Schedule>();
		Vector<Schedule> usr_s = new Vector<Schedule>();

		// Read in existing .cal's from export file
		ObjectInputStream fi;
		try {
			fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			schedules = (Vector<Schedule>) fi.readObject();
			fi.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		// Read in new users, checking for users that already exist in the database
		while (this.args_index < this.args.length) {
			try {
				String user = this.args[this.args_index ++] + ".cal";
				fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(user)));

				Schedule s = (Schedule) fi.readObject();
				if (!this.existingExport(s, schedules))
					usr_s.add(s);
				else
					System.out.println("W: Duplicate entry on " + s.name + ". Skipping.");

				fi.close();
			} catch (Exception e) {
				System.out.println("W: Could not find file. Skipping \"" + this.args[this.args_index - 1] + "\"");
			}
		}

		if (usr_s.size() == 0) {
			System.out.println("E: No files found. Nothing to do.");
			return;
		}

		// Merge
		schedules.addAll(usr_s);

		// Sort
		this.sort(schedules);

		// Print new users
		for (Schedule s : schedules) {
			System.out.println(s.name);
		}

		// Write out buffer to combined *.ccl file
		ObjectOutputStream fo;
		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			fo.writeObject(schedules);
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Reads in an export file and breaks it apart into individual user files, saving each
	@SuppressWarnings("unchecked")
	private void importx() {
		if (this.args.length < 2) {
			System.out.println("E: Insufficient arguments.");
			return;
		}

		String file = this.args[this.args_index ++] + ".ccl";

		Vector<Schedule> schedules = new Vector<Schedule>();

		// Read in existing .cal's from export file
		ObjectInputStream fi;
		try {
			fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			schedules = (Vector<Schedule>) fi.readObject();
			fi.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		// Print existing users
		for (Schedule s : schedules) {
			System.out.println(s.name);

			ObjectOutputStream fo;
			try {
				fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(s.name + ".cal")));
				fo.writeObject(s);
				fo.close();
			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				System.exit(-1);
			}
		}
	}

	// Merges the contents of two or more .ccl files into one .ccl file
	@SuppressWarnings("unchecked")
	private void merge() {
		if (this.args.length < 4) {
			System.out.println("E: Insufficient arguments");
			return;
		}

		String save = this.args[this.args_index ++];
		Vector<Schedule> schedules = new Vector<Schedule>();
		while (this.args_index < this.args.length) {
			ObjectInputStream fi;
			try {
				fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.args[this.args_index ++] + ".ccl")));
				schedules.addAll((Vector<Schedule>) fi.readObject());
				fi.close();
			} catch (Exception e) {
				System.out.println("W: Could not find file. Skipping \"" + this.args[this.args_index - 1] + ".ccl\"");
			}
		}

		if (schedules.size() == 0) {
			System.out.println("E: No files found. Nothing to do.");
			return;
		}

		this.sort(schedules);

		ObjectOutputStream fo;
		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(save + ".ccl")));
			fo.writeObject(schedules);
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	private void jtop() {
		String dir = "Python Versions/";
		Vector<String> files = this.findFilesWithExtension(".cal");
		(new File(dir)).mkdir();

		for (String file : files) {
			try {
				if (!file.endsWith(".cal"))
					file += ".cal";

				ObjectInputStream fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				Schedule s = (Schedule) fi.readObject();
				fi.close();

				BufferedWriter fo = new BufferedWriter(new FileWriter(dir + file));
				fo.write("#" + file + "\n");
				for (BusyTime bt : s.classes) {
					switch (bt.day) {
						case Monday:
							fo.write("m ");
							break;
						case Tuesday:
							fo.write("t ");
							break;
						case Wednesday:
							fo.write("w ");
							break;
						case Thursday:
							fo.write("r ");
							break;
						case Friday:
							fo.write("f ");
							break;
						default:
							break;
					}

					fo.write(bt.time.toString() + "\n");
				}
				fo.close();
			} catch (Exception e) {}
		}
	}

	private void ptoj() {

	}

	//                      //
	//  HELPER FUNCTIONS    //
	//                      //

	// Parses arguments for supported tokens
	private void readArgs() {
		if (this.args.length < 1) {
			System.out.println("E: No Arguments");
			this.state = State.HELP;
			return;
		}

		if (this.argsEquals("-u", "--adduser")) {
			this.args_index ++;
			this.state = State.ADDUSER;
			return;
		}

		if (this.argsEquals("-c", "--addclass")) {
			this.args_index ++;
			this.state = State.ADDCLASS;
			return;
		}

		if (this.argsEquals("-d", "--days")) {
			this.args_index ++;
			this.state = State.SETDAY;
			return;
		}

		if (this.argsEquals("-t", "--time")) {
			this.args_index ++;
			this.state = State.SETTIME;
			return;
		}

		if (this.argsEquals("-r", "--removeclass")) {
			this.args_index ++;
			this.state = State.REMOVECLASS;
			return;
		}

		if (this.argsEquals("-s", "--show")) {
			this.args_index ++;
			this.state = State.SHOW;
			return;
		}

		if (this.argsEquals("-e", "--export")) {
			this.args_index ++;
			this.state = State.EXPORT;
			return;
		}

		if (this.argsEquals("-l", "--list")) {
			this.args_index ++;
			this.state = State.LIST;
			return;
		}

		if (this.argsEquals("-x", "--addexport")) {
			this.args_index ++;
			this.state = State.ADDEXPORT;
			return;
		}

		if (this.argsEquals("-i", "--import")) {
			this.args_index ++;
			this.state = State.IMPORT;
			return;
		}

		if (this.argsEquals("-m", "--merge")) {
			this.args_index ++;
			this.state = State.MERGE;
			return;
		}

		if (this.argsEquals("-j", "--jtop")) {
			this.args_index ++;
			this.state = State.JTOP;
			return;
		}

		if (this.argsEquals("-p", "--ptoj")) {
			this.args_index ++;
			this.state = State.PTOJ;
			return;
		}

		if (this.argsEquals("-h", "--help")) {
			this.state = State.HELP;
			return;
		}

		this.state = State.ERR;
	}

	// Simplifies checking of args against supported tokens
	private boolean argsEquals(String s, String l) {
		return (this.args[this.args_index].equalsIgnoreCase(s) || this.args[this.args_index].equalsIgnoreCase(l));
	}

	// Allows useful calls to setDay from inside the program
	private void setDayInternal(String days) {
		this.days = new Vector<Day>();

		for (int i = 0; i < days.length(); i ++) {
			char c = days.charAt(i);

			if (c == 'm' || c == 'M') {
				this.days.add(Day.Monday);
			}

			if (c == 't' || c == 'T') {
				this.days.add(Day.Tuesday);
			}

			if (c == 'w' || c == 'W') {
				this.days.add(Day.Wednesday);
			}

			if (c == 'r' || c == 'R') {
				this.days.add(Day.Thursday);
			}

			if (c == 'f' || c == 'F') {
				this.days.add(Day.Friday);
			}
		}
	}

	// Allows useful calls to setTime from inside the program
	private void setTimeInternal(String time) {
		String delimiters = "[-:]";
		String[] times = time.split(delimiters);

		int starth = 0;
		int startm = 0;
		int endh = 0;
		int endm = 0;

		// Parse numbers from time input
		try {
			starth = Integer.parseInt(times[0]);
			startm = Integer.parseInt(times[1]);
			endh = Integer.parseInt(times[2]);
			endm = Integer.parseInt(times[3]);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		if (starth < 8)
			starth += 12;

		if (endh < 8)
			endh += 12;

		if (starth > 12 && endh < 12)
			endh += 12;

		if (endh < starth) {
			System.out.println("E: end < start");
			return;
		}

		this.time.start_hour = starth;
		this.time.start_minute = startm;
		this.time.end_hour = endh;
		this.time.end_minute = endm;
	}

	// Builds a vector of BusyTime's out of set Day's and Time's
	private Vector<BusyTime> buildBusyTimes() {
		Vector<BusyTime> btime = new Vector<BusyTime>();
		for (int i = 0; i < this.days.size(); i ++) {
			btime.add(new BusyTime());
			btime.get(i).day = this.days.get(i);
			btime.get(i).time = this.time;
		}

		return btime;
	}

	// Formats output for the show function
	private void showHelper(String day, Vector<BusyTime> bt) {
		System.out.print(day + "|");
		int index = 0;
		for (int i = 8; i < 20; i ++) {
			if (index >= bt.size())
				break;
			else
				if (bt.get(index).isBusy(i)) {
					System.out.print("X");
					if (bt.get(index).time.end_hour == i)
						index ++;
				}
				else
					System.out.print(" ");
		}
		System.out.println();
	}

	// Writes anything in the schedule vector to file <name>.ccl
	private void writeSchedulesExport(String name, Vector<Schedule> schedules) {
		ObjectOutputStream fo;
		try {
			fo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(name + ".ccl")));
			fo.writeObject(schedules);
			fo.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}
	}

	// Checks to see if a certain schedule exits in an export file
	private boolean existingExport(Schedule test, Vector<Schedule> schedules) {
		for (Schedule s : schedules) {
			if (s.equals(test)) {
				return true;
			}
		}
		return false;
	}

	// Sorts the vector of schedules by name
	private void sort(Vector<Schedule> schedules) {
		for (int i = 0; i < schedules.size(); i ++) {
			for (int j = 0; j < schedules.size() - 1; j ++) {
				Schedule tmp0 = schedules.get(j);
				Schedule tmp1 = schedules.get(j + 1);
				if (tmp0.name.compareToIgnoreCase(tmp1.name) > 0) {
					schedules.set(j, tmp1);
					schedules.set(j + 1, tmp0);
				}
			}
		}
	}

	// Finds all the files in the current directory that end with <extension>
	private Vector<String> findFilesWithExtension(String extension) {
		String path = ".";
		File folder = new File(path);
		File[] list = folder.listFiles();
		Vector<String> files = new Vector<String>();

		for (File f : list) {
			if (f.isFile() && f.getName().endsWith(extension)) {
				String name = f.getName();
				name = name.substring(0, name.length() - 4);
				files.add(name);
			}
		}

		return files;
	}
}
