package pkg;

import java.io.Serializable;
import java.util.Vector;

public class Schedule implements Serializable{
	private static final long serialVersionUID = 1L;
	public Vector<BusyTime> classes = new Vector<BusyTime>(0);
	public String name = "";
	public static final int CLASS_VERSION = 2;

	public void sort() {
		if (this.classes.size() < 2)
			return;

		for (int i = 0; i < this.classes.size(); i ++) {
			for (int j = 0; j < this.classes.size() - 1; j ++) {
				if (this.classes.get(j).compareTo(this.classes.get(j + 1)) > 0) {
					BusyTime tmp0 = this.classes.get(j);
					BusyTime tmp1 = this.classes.get(j + 1);
					this.classes.set(j, tmp1);
					this.classes.set(j + 1, tmp0);
				}
			}
		}
	}

	@Override
	public String toString(){
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Schedule) {
			Schedule s = (Schedule) obj;
			return (s.name.equals(this.name) && s.classes.equals(this.classes));
		}

		return false;
	}
}
