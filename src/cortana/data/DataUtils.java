package cortana.data;

import java.util.HashSet;
import java.util.Set;

public class DataUtils {
	/* calculate the difference between two sets */
	public static Set difference(Set a, Set b) {
        Set temp = new HashSet(a);
		temp.removeAll(b);
		return temp;
	}

	/* calculate the intersection of two sets */
	public static Set intersection(Set a, Set b) {
        Set temp = new HashSet(a);
		temp.retainAll(b);
		return temp;
	}
}
