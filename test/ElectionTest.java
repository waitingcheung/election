import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import election.Election;
import election.Logger;

public class ElectionTest {

	@Test
	public void ParseInputs() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		Election election = new Election(seats, party1, party2);
		ArrayList<ImmutablePair<String, Integer>> p1 = election.getParty1();
		ArrayList<ImmutablePair<String, Integer>> p2 = election.getParty2();

		assertTrue(election.getSeats() > 0);
		assertTrue(p1.size() > 0);
		assertTrue(p2.size() > 0);

		for (ImmutablePair<String, Integer> candidate : p1)
			assertFalse(candidate.getLeft().contains("("));

		for (ImmutablePair<String, Integer> candidate : p2)
			assertFalse(candidate.getLeft().contains("("));
	}

	@Test
	public void CountVotes() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		Election election = new Election(seats, party1, party2);
		assertEquals(election.getVotes(), 9);
	}

	@Test
	public void CountQuota() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		Election election = new Election(seats, party1, party2);
		assertEquals(election.getQuota(), 4);
	}

	@Test
	public void CountVotesForList() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		Election election = new Election(seats, party1, party2);
		assertEquals(election.countVotesForList(Arrays.asList(new String[] { "c11" })), 3);
		assertEquals(election.countVotesForList(Arrays.asList(new String[] { "c12", "c13" })), 3);
		assertEquals(election.countVotesForList(Arrays.asList(new String[] { "c21", "c22" })), 3);
	}

	@Test
	public void CountSeatsForFirstRound() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		{
			Election election = new Election(seats, party1, party2);
			assertEquals(election.seatsFirstRound(Arrays.asList(new String[] { "c11" })), 0);
			assertEquals(election.seatsFirstRound(Arrays.asList(new String[] { "c12", "c13" })), 0);
			assertEquals(election.seatsFirstRound(Arrays.asList(new String[] { "c21", "c22" })), 0);
		}

		{
			Election election = new Election(seats, party1, party2);
			assertEquals(election.seatsFirstRound(Arrays.asList(new String[] { "c11", "c12", "c13" })), 1);
			assertEquals(election.seatsFirstRound(Arrays.asList(new String[] { "c21", "c22" })), 0);
		}
	}

	@Test
	public void CountSeatsForSecondRound() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		// Strategy profile 1
		{
			Election election = new Election(seats, party1, party2);

			List<ArrayList<String>> strategy1 = new ArrayList<ArrayList<String>>();
			List<ArrayList<String>> strategy2 = new ArrayList<ArrayList<String>>();

			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c11" })));
			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c12", "c13" })));
			strategy2.add(new ArrayList<String>(Arrays.asList(new String[] { "c21", "c22" })));

			assert new BigFraction(2, 3)
					.equals(election.seatsSecondRound(Arrays.asList(new String[] { "c11" }), strategy1, strategy2));
			assert new BigFraction(2, 3).equals(
					election.seatsSecondRound(Arrays.asList(new String[] { "c12", "c13" }), strategy1, strategy2));
			assert new BigFraction(2, 3).equals(
					election.seatsSecondRound(Arrays.asList(new String[] { "c21", "c22" }), strategy1, strategy2));
		}

		{
			Election election = new Election(seats, party1, party2);

			List<ArrayList<String>> strategy1 = new ArrayList<ArrayList<String>>();
			List<ArrayList<String>> strategy2 = new ArrayList<ArrayList<String>>();

			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c11", "c12", "c13" })));
			strategy2.add(new ArrayList<String>(Arrays.asList(new String[] { "c21", "c22" })));

			assert new BigFraction(0).equals(election
					.seatsSecondRound(Arrays.asList(new String[] { "c11", "c12", "c13" }), strategy1, strategy2));
			assert new BigFraction(1).equals(
					election.seatsSecondRound(Arrays.asList(new String[] { "c21", "c22" }), strategy1, strategy2));
		}
	}

	@Test
	public void ComputePayOff() {
		String seats = "2";
		String party1 = "(c11,3),(c12,2),(c13,1)";
		String party2 = "(c21,2),(c22,1)";

		// Strategy profile 1
		{
			Election election = new Election(seats, party1, party2);

			ArrayList<ArrayList<String>> strategy1 = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> strategy2 = new ArrayList<ArrayList<String>>();

			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c11" })));
			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c12", "c13" })));
			strategy2.add(new ArrayList<String>(Arrays.asList(new String[] { "c21", "c22" })));

			ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
					strategy1, strategy2);

			assert new BigFraction(4, 3).equals(election.payoff(profilePair, 1));
			assert new BigFraction(2, 3).equals(election.payoff(profilePair, 2));
		}

		// Strategy profile 2
		{
			Election election = new Election(seats, party1, party2);

			ArrayList<ArrayList<String>> strategy1 = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> strategy2 = new ArrayList<ArrayList<String>>();

			strategy1.add(new ArrayList<String>(Arrays.asList(new String[] { "c11", "c12", "c13" })));
			strategy2.add(new ArrayList<String>(Arrays.asList(new String[] { "c21", "c22" })));

			ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
					strategy1, strategy2);

			assert new BigFraction(1).equals(election.payoff(profilePair, 1));
			assert new BigFraction(1).equals(election.payoff(profilePair, 2));
		}
	}

	/*
	 * @Test public void SortProfilePairs() { String seats = "1"; String party1
	 * = "(a1,85),(a2,478),(a3,991),(a4,74),(a5,575)"; String party2 =
	 * "(b1,745),(b2,170),(b3,28),(b4,278)";
	 * 
	 * Election election = new Election(seats, party1, party2);
	 * 
	 * long startTime = System.nanoTime();
	 * 
	 * election.generateAllProfilePairs();
	 * 
	 * long stopTime = System.nanoTime(); double elapsedTime = (double)
	 * TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS)
	 * / 1000;
	 * 
	 * System.out.println("# Time for partition generation and sorting(s): " +
	 * elapsedTime);
	 * 
	 * ArrayList<ImmutablePair<ArrayList<ArrayList<String>>,
	 * ArrayList<ArrayList<String>>>> allProfilePairs =
	 * election.getAllProfilePairs(); for (int i = 0; i < allProfilePairs.size()
	 * - 1; i++) { ImmutablePair<ArrayList<ArrayList<String>>,
	 * ArrayList<ArrayList<String>>> profilePair1 = allProfilePairs.get(i);
	 * ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>
	 * profilePair2 = allProfilePairs.get(i + 1);
	 * 
	 * int diff1 = Math.abs(profilePair1.getLeft().size() -
	 * profilePair1.getRight().size()); int diff2 =
	 * Math.abs(profilePair2.getLeft().size() - profilePair2.getRight().size());
	 * int sum1 = profilePair1.getLeft().size() +
	 * profilePair1.getRight().size(); int sum2 = profilePair1.getLeft().size()
	 * + profilePair1.getRight().size();
	 * 
	 * if (diff1 == diff2) assertTrue(sum1 >= sum2); else assertTrue(diff1 <
	 * diff2); } }
	 */

	/*
	 * @Test public void ComputeNashEquilibrium() { { String seats = "2"; String
	 * party1 = "(c11,3),(c12,2),(c13,1)"; String party2 = "(c21,2),(c22,1)";
	 * 
	 * Election election = new Election(seats, party1, party2);
	 * election.generateAllProfilePairs();
	 * election.generateAllNashEquilibrium();
	 * 
	 * assertTrue(Logger.getSize() > 0);
	 * 
	 * for (ImmutablePair<ArrayList<ArrayList<String>>,
	 * ArrayList<ArrayList<String>>> ne : allNashEquilibrium) {
	 * assertEquals(election.payoff(ne, 1), new BigFraction(4, 3));
	 * assertEquals(election.payoff(ne, 2), new BigFraction(2, 3)); } } { String
	 * seats = "2"; String party1 =
	 * "(a1,85),(a2,478),(a3,991),(a4,74),(a5,575)"; String party2 =
	 * "(b1,745),(b2,170),(b3,28),(b4,278)";
	 * 
	 * Election election = new Election(seats, party1, party2);
	 * election.generateAllProfilePairs();
	 * election.generateAllNashEquilibrium();
	 * 
	 * ArrayList<ImmutablePair<ArrayList<ArrayList<String>>,
	 * ArrayList<ArrayList<String>>>> allNashEquilibrium =
	 * election.getAllNashEquilibrium(); assertTrue(allNashEquilibrium.size() >
	 * 0);
	 * 
	 * for (ImmutablePair<ArrayList<ArrayList<String>>,
	 * ArrayList<ArrayList<String>>> ne: allNashEquilibrium) {
	 * assertEquals(election.payoff(ne, 1), new BigFraction(1));
	 * assertEquals(election.payoff(ne, 2), new BigFraction(1)); } } }
	 */

	@Test
	public void CountNashEquilibrium() {
		{
			Logger.reset();
			String seats = "1";
			String party1 = "(a1,85),(a2,478),(a3,991),(a4,74),(a5,575)";
			String party2 = "(b1,745),(b2,170),(b3,28),(b4,278)";

			Election election = new Election(seats, party1, party2);
			election.generateAllProfilePairs();
			int neCount = 0;

			ArrayList<ArrayList<ArrayList<String>>> allStrategies1 = election.getAllStrategies1();
			ArrayList<ArrayList<ArrayList<String>>> allStrategies2 = election.getAllStrategies2();
			ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>> searchSpace = new ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>>(
					allStrategies1, allStrategies2);

			for (ArrayList<ArrayList<String>> strategy1 : allStrategies1) {
				for (ArrayList<ArrayList<String>> strategy2 : allStrategies2) {
					ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> pair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
							strategy1, strategy2);
					if (election.isFeasible(pair, searchSpace))
						neCount++;
				}
			}

			election.generateAllNashEquilibrium();
			assertEquals(Logger.getSize(), neCount);
		}
		{
			Logger.reset();
			String seats = "2";
			String party1 = "(a1,85),(a2,478),(a3,991),(a4,74),(a5,575)";
			String party2 = "(b1,745),(b2,170),(b3,28),(b4,278)";

			Election election = new Election(seats, party1, party2);
			election.generateAllProfilePairs();
			int neCount = 0;

			ArrayList<ArrayList<ArrayList<String>>> allStrategies1 = election.getAllStrategies1();
			ArrayList<ArrayList<ArrayList<String>>> allStrategies2 = election.getAllStrategies2();
			ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>> searchSpace = new ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>>(
					allStrategies1, allStrategies2);

			for (ArrayList<ArrayList<String>> strategy1 : allStrategies1) {
				for (ArrayList<ArrayList<String>> strategy2 : allStrategies2) {
					ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> pair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
							strategy1, strategy2);
					if (election.isFeasible(pair, searchSpace))
						neCount++;
				}
			}

			election.generateAllNashEquilibrium();
			assertEquals(Logger.getSize(), neCount);
		}
		{
			Logger.reset();
			String seats = "3";
			String party1 = "(a1,85),(a2,478),(a3,991),(a4,74),(a5,575)";
			String party2 = "(b1,745),(b2,170),(b3,28),(b4,278)";

			Election election = new Election(seats, party1, party2);
			election.generateAllProfilePairs();
			int neCount = 0;

			ArrayList<ArrayList<ArrayList<String>>> allStrategies1 = election.getAllStrategies1();
			ArrayList<ArrayList<ArrayList<String>>> allStrategies2 = election.getAllStrategies2();
			ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>> searchSpace = new ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>>(
					allStrategies1, allStrategies2);

			for (ArrayList<ArrayList<String>> strategy1 : allStrategies1) {
				for (ArrayList<ArrayList<String>> strategy2 : allStrategies2) {
					ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> pair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
							strategy1, strategy2);
					if (election.isFeasible(pair, searchSpace))
						neCount++;
				}
			}

			election.generateAllNashEquilibrium();
			assertEquals(Logger.getSize(), neCount);
		}
	}
}
