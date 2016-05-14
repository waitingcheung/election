import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import election.Election;
import election.Partition;

public class PartitionTest {

	@Test
	public void test() {
		String seats = "1";
		String party1 = "(a1,57),(a2,270),(a3,875),(a4,986),(a5,394),(a6,772),(a7,453),(a8,152),(a9,881),(a10,310)";
		String party2 = "(b1,697),(b2,529),(b3,562),(b4,284),(b5,399),(b6,583),(b7,219),(b8,36),(b9,119),(b10,67)";

		Election election = new Election(seats, party1, party2);
		ArrayList<ImmutablePair<String, Integer>> p1 = election.getParty1();

		long startTime = System.nanoTime();

		Partition partition = new Partition(p1);

		long stopTime = System.nanoTime();
		double elapsedTime = (double) TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS) / 1000;

		System.out.println("# Time for partition generation(s): " + elapsedTime);

		assertEquals(partition.getStrategies().size(), 115975);
	}

}
