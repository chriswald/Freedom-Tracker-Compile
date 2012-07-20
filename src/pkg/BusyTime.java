package pkg;

import java.io.Serializable;

public class BusyTime implements Serializable{
	private static final long serialVersionUID = 1L;
	public Day day = null;
	public Time time = new Time();
	
	public boolean isBusy(int hour) {
		return (hour >= time.start_hour && hour <= time.end_hour);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BusyTime) {
			BusyTime b = (BusyTime) obj;

			return (b.day == this.day && b.time.equals(this.time));
		}

		return false;
	}

	@Override
	public String toString() {
		String temp = "";
		if (this.day == Day.Monday)
			temp += "Monday ";
		if (this.day == Day.Tuesday)
			temp += "Tuesday ";
		if (this.day == Day.Wednesday)
			temp += "Wednesday ";
		if (this.day == Day.Thursday)
			temp += "Thursday ";
		if (this.day == Day.Friday)
			temp += "Friday ";

		temp += this.time.toString();

		return temp;
	}

	public int compareTo(Object obj) {
		BusyTime bt = (BusyTime) obj;

		if (this.day.compareTo(bt.day) < 0)
			return -1;
		else if (this.day.compareTo(bt.day) > 0)
			return 1;

		if (this.time.compareTo(bt.time) < 0)
			return -1;
		if (this.time.compareTo(bt.time) > 0)
			return 1;

		return 0;
	}
}
