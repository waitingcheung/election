// Adapted from https://compprog.wordpress.com/2007/10/15/generating-the-partitions-of-a-set/

package election;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class Partition {
	private ArrayList<ImmutablePair<String, Integer>> candidates = new ArrayList<ImmutablePair<String, Integer>>();
	private ArrayList<ArrayList<ArrayList<String>>> strategies = new ArrayList<ArrayList<ArrayList<String>>>();
	/*
	 * s[i] is the number of the set in which the ith element should go
	 */
	private ArrayList<Integer> s = new ArrayList<Integer>();

	/* m[i] is the largest of the first i elements in s */
	private ArrayList<Integer> m = new ArrayList<Integer>();

	public Partition(ArrayList<ImmutablePair<String, Integer>> inputs) {
		candidates.addAll(inputs);

		/*
		 * The first way to partition a set is to put all the elements in the
		 * same subset.
		 */
		for (int i = 0; i < candidates.size(); ++i) {
			s.add(i, 1);
			m.add(i, 1);
		}

		/* Print the first partitioning. */
		printp();

		/* Print the other partitioning schemes. */
		while (next())
			printp();
	}

	/*
	 * printp - print out the partitioning scheme s of n elements as: {1, 2, 4}
	 * {3}
	 */
	private void printp() {
		final int n = candidates.size();

		/* Get the total number of partitions. In the exemple above, 2. */
		int part_num = 1;
		int i;
		for (i = 0; i < n; ++i)
			if (s.get(i) > part_num)
				part_num = s.get(i);

		ArrayList<ArrayList<String>> profile = new ArrayList<ArrayList<String>>();

		/* Print the p partitions. */
		int p;
		for (p = part_num; p >= 1; --p) {
			// System.out.print("{");
			ArrayList<String> list = new ArrayList<String>();

			/* If s[i] == p, then i + 1 is part of the pth partition. */
			for (i = 0; i < n; ++i)
				if (s.get(i) == p) {
					// System.out.print(i + 1);
					// System.out.print(":" + candidates.get(i) + " ");
					list.add(candidates.get(i).getKey());
				}
			profile.add(list);
			// System.out.print("} ");
		}

		strategies.add(profile);
		// System.out.print("\n");
	}

	/*
	 * next - given the partitioning scheme represented by s and m, generate the
	 * next
	 * 
	 * Returns: 1, if a valid partitioning was found 0, otherwise
	 */
	private boolean next() {
		final int n = candidates.size();

		/*
		 * Update s: 1 1 1 1 -> 2 1 1 1 -> 1 2 1 1 -> 2 2 1 1 -> 3 2 1 1 -> 1 1
		 * 2 1 ...
		 */
		/*
		 * int j; System.out.println(" -> ("); for (j = 0; j &lt; n; ++j)
		 * System.out.println("%d, ", s[j]); System.out.println("\\b\\b)\\n");
		 */
		int i = 0;
		s.set(i, s.get(i) + 1);
		while ((i < n - 1) && (s.get(i) > m.get(i) + 1)) {
			s.set(i, 1);
			++i;
			s.set(i, s.get(i) + 1);
		}

		/*
		 * If i is has reached n-1 th element, then the last unique partitiong
		 * has been found
		 */
		if (i == n - 1)
			return false;

		/*
		 * Because all the first i elements are now 1, s[i] (i + 1 th element)
		 * is the largest. So we update max by copying it to all the first i
		 * positions in m.
		 */
		int max = s.get(i);
		if (m.get(i) > max)
			max = m.get(i);
		for (i = i - 1; i >= 0; --i)
			m.set(i, max);

		/*
		 * for (i = 0; i &lt; n; ++i) System.out.println("%d ", m[i]);
		 * getchar();
		 */
		return true;
	}

	public ArrayList<ArrayList<ArrayList<String>>> getStrategies() {
		return strategies;
	}
}
