package pkg;

import java.io.Serializable;

public class Time implements Serializable{
	private static final long serialVersionUID = 1L;
	public int start_hour;
	public int start_minute;
	public int end_hour;
	public int end_minute;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Time){
			Time t = (Time) obj;

			return (t.start_hour == this.start_hour && t.start_minute == this.start_minute &&
					t.end_hour == this.end_hour && t.end_minute == this.end_minute);
		}

		return false;
	}

	@Override
	public String toString() {
		String temp = "";
		temp += this.start_hour + ":";
		if (this.start_minute < 10)
			temp += "0";
		temp += this.start_minute + "-" + this.end_hour + ":";
		if (this.end_minute < 10)
			temp += "0";
		temp += this.end_minute + "";

		return temp;
	}

	public int compareTo(Object o) {
		Time t = (Time) o;

		if (this.start_hour < t.start_hour)
			return -1;
		else if (this.start_hour > t.start_hour)
			return 1;
		else
			if (this.start_minute < t.start_minute)
				return -1;
			else if (this.start_minute > t.start_minute)
				return 1;

		return 0;

	}
}
