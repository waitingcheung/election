package election;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.fraction.BigFraction;

public class Election {
	private int s = -1; // number of seats
	
	// number of seats left after the first round
	private int remainingSeats = -1; 
	private int votes = -1; // number of votes
	private int quota = -1; // number of quota

	// a set of candidates in party 1
	private ArrayList<ImmutablePair<String, Integer>> party1 = new ArrayList<ImmutablePair<String, Integer>>();
	
	// a set of candidates in party 1
	private ArrayList<ImmutablePair<String, Integer>> party2 = new ArrayList<ImmutablePair<String, Integer>>();
	
	private ArrayList<ArrayList<ArrayList<String>>> allStrategies1 = null;
	private ArrayList<ArrayList<ArrayList<String>>> allStrategies2 = null;

	// candidate -> votes
	private LinkedHashMap<String, Integer> voteMap = new LinkedHashMap<String, Integer>();
	
	// candidate -> party
	private LinkedHashMap<String, Integer> partyMap = new LinkedHashMap<String, Integer>();
	
	// list -> votes
	private LinkedHashMap<List<String>, Integer> votesForListMap = new LinkedHashMap<List<String>, Integer>();
	
	// profile pair -> payoff
	public static ArrayList<LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction>> payoffMap = null;

	private LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, Boolean> prunedProfilePair1 = new LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, Boolean>();
	private LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, Boolean> prunedProfilePair2 = new LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, Boolean>();
	
	public Election(String seats, String party1, String party2) {
		parseInputs(seats, party1, party2);
		countVotes();
		quota = (int) Math.floor(votes / s);

		payoffMap = new ArrayList<LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction>>();
		LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction> payoffMap1 = new LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction>();
		LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction> payoffMap2 = new LinkedHashMap<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>, BigFraction>();
		payoffMap.add(payoffMap1);
		payoffMap.add(payoffMap2);
	}

	private void parseInputs(String seats, String p1, String p2) {
		s = Integer.parseInt(seats);
		parseParty(party1, p1);
		parseParty(party2, p2);
		mapPartiesAndVotes();
	}

	private void parseParty(ArrayList<ImmutablePair<String, Integer>> party, String partyRaw) {
		List<String> raw = Arrays.asList(partyRaw.split(","));
		for (int i = 0; i < raw.size(); i += 2) {
			String id = raw.get(i).trim().substring(1);
			int votes = Integer.parseInt(raw.get(i + 1).trim().replace(")", ""));
			ImmutablePair<String, Integer> candidate = new ImmutablePair<String, Integer>(id, votes);
			party.add(candidate);
		}
	}

	private void mapPartiesAndVotes() {
		for (ImmutablePair<String, Integer> candidate : party1) {
			String candidateId = candidate.getLeft();
			partyMap.put(candidateId, 1);
			voteMap.put(candidateId, candidate.getRight());
		}
		for (ImmutablePair<String, Integer> candidate : party2) {
			String candidateId = candidate.getLeft();
			partyMap.put(candidateId, 2);
			voteMap.put(candidateId, candidate.getRight());
		}
	}

	private void countVotes() {
		votes = 0;
		for (ImmutablePair<String, Integer> candidate : party1)
			votes += candidate.getRight();
		for (ImmutablePair<String, Integer> candidate : party2)
			votes += candidate.getRight();
	}

	public int countVotesForList(List<String> list) {
		if (votesForListMap.containsKey(list))
			return votesForListMap.get(list);
		
		int listVotes = 0;
		for (String candidate : list)
			listVotes += voteMap.get(candidate);
		
		votesForListMap.put(list, listVotes);
		return listVotes;
	}

	private int newCountVotesForList(List<String> list) {
		return countVotesForList(list) - seatsFirstRound(list) * quota;
	}

	public BigFraction payoff(ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair,
			int party) {
		if (payoffMap.get(party - 1).containsKey(profilePair))
			return payoffMap.get(party - 1).get(profilePair);

		BigFraction seatsWon = new BigFraction(0);
		ArrayList<ArrayList<String>> strategy1 = profilePair.getLeft();
		ArrayList<ArrayList<String>> strategy2 = profilePair.getRight();

		if (party == 1) {
			for (ArrayList<String> list : strategy1)
				seatsWon = seatsWon.add(expectedSeats(list, strategy1, strategy2));
		} else if (party == 2) {
			for (ArrayList<String> list : strategy2)
				seatsWon = seatsWon.add(expectedSeats(list, strategy1, strategy2));
		}

		payoffMap.get(party - 1).put(profilePair, seatsWon);
		return seatsWon;
	}

	public BigFraction expectedSeats(List<String> list, List<ArrayList<String>> strategy1,
			List<ArrayList<String>> strategy2) {
		int seatsFirst = seatsFirstRound(list);
		BigFraction seatsSecond = seatsSecondRound(list, strategy1, strategy2);
		seatsSecond = seatsSecond.add(seatsFirst);
		return seatsSecond;
	}

	public int seatsFirstRound(List<String> list) {
		int candidateCount = list.size();
		int voteCount = countVotesForList(list);
		return Math.min(candidateCount, (int) Math.floor(voteCount / quota));
	}

	public BigFraction seatsSecondRound(List<String> list, List<ArrayList<String>> strategy1,
			List<ArrayList<String>> strategy2) {
		remainingSeats = s;
		ArrayList<ArrayList<String>> allNewStrategies = computeNewList(strategy1, strategy2);

		int votesGreaterThanOrEqual = 0;
		for (ArrayList<String> newList : allNewStrategies) {
			if (newCountVotesForList(newList) >= newCountVotesForList(list))
				votesGreaterThanOrEqual++;
		}

		if (votesGreaterThanOrEqual <= remainingSeats)
			return new BigFraction(1);
		else if (votesGreaterThanOrEqual > remainingSeats) {
			int votesGreaterThan = 0;
			for (ArrayList<String> newList : allNewStrategies) {
				if (newCountVotesForList(newList) > newCountVotesForList(list))
					votesGreaterThan++;
			}

			if (votesGreaterThan < remainingSeats) {
				int votesEqual = 0;
				for (ArrayList<String> newList : allNewStrategies) {
					if (newCountVotesForList(newList) == newCountVotesForList(list))
						votesEqual++;
				}
				return new BigFraction(remainingSeats - votesGreaterThan, votesEqual);
			} else
				return new BigFraction(0);
		} else
			return new BigFraction(0);
	}

	private ArrayList<ArrayList<String>> computeNewList(List<ArrayList<String>> strategy1, List<ArrayList<String>> strategy2) {
		ArrayList<ArrayList<String>> newStrategy = new ArrayList<ArrayList<String>>();
				
		for (ArrayList<String> list : strategy1) {
			int seatsWon = seatsFirstRound(list);
			remainingSeats -= seatsWon;
			if (list.size() > seatsWon)
				newStrategy.add(list);
		}
		
		for (ArrayList<String> list : strategy2) {
			int seatsWon = seatsFirstRound(list);
			remainingSeats -= seatsWon;
			if (list.size() > seatsWon)
				newStrategy.add(list);
		}
		
		return newStrategy;
	}

	public void generateAllProfilePairs() {
		Partition p1 = new Partition(party1);
		Partition p2 = new Partition(party2);

		allStrategies1 = p1.getStrategies();
		allStrategies2 = p2.getStrategies();
		
		/*
		// Sort by increasing order, first |x1 - x2|, then (x1 + x2)
		Collections.sort(allProfilePairs,
				new Comparator<ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>>() {
					public int compare(
							ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair1,
							ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair2) {
						int diff1 = Math.abs(profilePair1.getLeft().size() - profilePair1.getRight().size());
						int diff2 = Math.abs(profilePair2.getLeft().size() - profilePair2.getRight().size());
						int sum1 = profilePair1.getLeft().size() + profilePair1.getRight().size();
						int sum2 = profilePair1.getLeft().size() + profilePair1.getRight().size();

						if (diff1 == diff2) {
							if (sum1 == sum2) {
								return 0;
							} else if (sum1 > sum2) {
								return -1;
							} else {
								return 1;
							}
						} else if (diff1 < diff2) {
							return -1;
						} else {
							return 1;
						}
					}
		});
		*/
	}

	public void generateAllNashEquilibrium() {		
		for (int size1 = 1; size1 <= party1.size(); size1++) {
			ArrayList<ArrayList<ArrayList<String>>> allProfile1 = getAllProfilePairsOfSize(allStrategies1, size1, 1);

			for (ArrayList<ArrayList<String>> profile1 : allProfile1) {
				ArrayList<ArrayList<ArrayList<String>>> notDominatedProfiles = computeNotConditionallyDominated(
						profile1, allStrategies2, 2);

				for (int size2 = 1; size2 <= party2.size(); size2++) {
					ArrayList<ArrayList<ArrayList<String>>> allProfile2 = getAllProfilePairsOfSize(notDominatedProfiles,
							size2, 2);
					
					for (ArrayList<ArrayList<String>> profile2 : allProfile2) {
							ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair = new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(
									profile1, profile2);
							
							ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>> searchSpace = new ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>>(allStrategies1, notDominatedProfiles);
							
							if (isFeasible(profilePair, searchSpace)) {
								StringBuilder sb = new StringBuilder();
								
								sb.append("(");
								sb.append(printProfile(profilePair.getLeft()));
								sb.append(",");
								sb.append(printProfile(profilePair.getRight()));
								sb.append(") payoff (");
								sb.append(printBigFraction(payoff(profilePair, 1)));
								sb.append(", ");
								sb.append(printBigFraction(payoff(profilePair, 2)));
								sb.append(")\n");
																 
								Logger.add(sb.toString());
							}
					}
				}
			}
		}		
	}

	private ArrayList<ArrayList<ArrayList<String>>> getAllProfilePairsOfSize(
			ArrayList<ArrayList<ArrayList<String>>> source, int size, int party) {
		ArrayList<ArrayList<ArrayList<String>>> targetProfilePairs = new ArrayList<ArrayList<ArrayList<String>>>();

		for (ArrayList<ArrayList<String>> profile : source) {
			if (profile.size() == size)
				targetProfilePairs.add(profile);
		}

		return targetProfilePairs;
	}

	private String printProfile(ArrayList<ArrayList<String>> profile) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		for (int i = 0; i < profile.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append("{");

			ArrayList<String> candidates = profile.get(i);
			for (int j = 0; j < candidates.size(); j++) {
				if (j > 0)
					sb.append(",");
				sb.append(candidates.get(j));
			}
			sb.append("}");
		}
		sb.append("}");
		return sb.toString();
	}
	
	private String printBigFraction(BigFraction bf) {
		int numerator = bf.getNumeratorAsInt();
		int denominator = bf.getDenominatorAsInt();
		
		if (numerator < denominator) {
			if (numerator == 0)
				return "0";
			else
				return numerator + "/" + denominator;
		} else {
			int remainder = numerator % denominator;
			if (remainder == 0)
				return "" + numerator / denominator;
			else
				return numerator / denominator + " " + remainder + "/" + denominator;
		}		
	}

	private ArrayList<ArrayList<ArrayList<String>>> computeNotConditionallyDominated(
			ArrayList<ArrayList<String>> profile, ArrayList<ArrayList<ArrayList<String>>> allStrategies, int party) {
		ArrayList<ArrayList<ArrayList<String>>> notDominatedProfiles = new ArrayList<ArrayList<ArrayList<String>>>();

		BigFraction maxP = new BigFraction(0);
		
		for (ArrayList<ArrayList<String>> strategy : allStrategies) {
			ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair = party == 2
					? new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(profile, strategy)
					: new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(strategy, profile);

			BigFraction p = payoff(profilePair, party);
			if (p.compareTo(maxP) == 1)
				maxP = p;
		}
		
		for (ArrayList<ArrayList<String>> strategy : allStrategies) {
			ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair = party == 2
					? new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(profile, strategy)
					: new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(strategy, profile);

			BigFraction p = payoff(profilePair, party);
			if (p.equals(maxP))
				notDominatedProfiles.add(strategy);
		}

		return notDominatedProfiles;
	}

	public boolean isFeasible(ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair,
			ImmutablePair<ArrayList<ArrayList<ArrayList<String>>>, ArrayList<ArrayList<ArrayList<String>>>> searchSpace) {

		return !betterStrategyExists(profilePair, 1) && !betterStrategyExists(profilePair, 2)
				&& isFeasibleForPlayer(profilePair, searchSpace.getLeft(), 1)
				&& isFeasibleForPlayer(profilePair, searchSpace.getRight(), 2)
				&& isPayoffSumCorrect(profilePair);
	}

	private boolean betterStrategyExists(ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair, int party) {
		ArrayList<ArrayList<String>> jointList = new ArrayList<ArrayList<String>>();
		jointList.addAll(profilePair.getLeft());
		jointList.addAll(profilePair.getRight());
		
		Collections.sort(jointList, new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> list1, ArrayList<String> list2) {
				int votes1 = countVotesForList(list1);
				int votes2 = countVotesForList(list2);
				
				if (votes1 > votes2)
					return -1;
				else if (votes1 < votes2)
					return 1;
				else
					return 0;
			}
		});
		
		if (s <= jointList.size()) {
			int cutoff = countVotesForList(jointList.get(s - 1));
			
			ArrayList<String> jointList1 = new ArrayList<String>();
			ArrayList<String> jointList2 = new ArrayList<String>();
			
			for (int i = s + 1; i < jointList.size(); i++) {
				ArrayList<String> currentList = jointList.get(i);
				int votes = countVotesForList(currentList);
				
				if (votes < cutoff) {
					if (party == 1)
						jointList1.addAll(currentList);
					else if (party == 2)
						jointList2.addAll(currentList);
				}
			}
			
			int votes = party == 1 ? countVotesForList(jointList1) : countVotesForList(jointList2);
			if (votes >= cutoff) {
				if (party == 1)
					prunedProfilePair1.put(profilePair, true); 
				else
					prunedProfilePair2.put(profilePair, true);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isFeasibleForPlayer(
			ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair,
			ArrayList<ArrayList<ArrayList<String>>> allStrategies, int party) {
		ArrayList<ArrayList<String>> profile = party == 1 ? profilePair.getLeft() : profilePair.getRight();
		ArrayList<ArrayList<String>> fixedProfile = party == 1 ? profilePair.getRight() : profilePair.getLeft();

		BigFraction p = payoff(profilePair, party);

		for (ArrayList<ArrayList<String>> strategy : allStrategies) {
			if (!profile.equals(strategy)) {
				ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> newProfilePair = party == 1
						? new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(strategy,
								fixedProfile)
						: new ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>>(fixedProfile,
								strategy);
				if (!isPruneProfilePair(newProfilePair, party)) {
					BigFraction newP = payoff(newProfilePair, party);
					if (p.compareTo(newP) == -1)
						return false;
				}
			}
		}

		return true;		
	}

	private boolean isPayoffSumCorrect(ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair) {
		BigFraction expected = new BigFraction(s);
		BigFraction actual = new BigFraction(0);
		
		BigFraction payoff1 = payoff(profilePair, 1);
		BigFraction payoff2 = payoff(profilePair, 2);
		actual = actual.add(payoff1);
		actual = actual.add(payoff2);
		
		return expected.equals(actual);
	}
	
	private boolean isPruneProfilePair(ImmutablePair<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> profilePair, int party) {
		if (party == 1)
			return prunedProfilePair1.containsKey(profilePair);
		else
			return prunedProfilePair2.containsKey(profilePair);		
	}

	public int getSeats() {
		return s;
	}

	public ArrayList<ImmutablePair<String, Integer>> getParty1() {
		return party1;
	}

	public ArrayList<ImmutablePair<String, Integer>> getParty2() {
		return party2;
	}

	public int getVotes() {
		return votes;
	}

	public int getQuota() {
		return quota;
	}
	
	public ArrayList<ArrayList<ArrayList<String>>> getAllStrategies1() {
		return allStrategies1;
	}
	
	public ArrayList<ArrayList<ArrayList<String>>> getAllStrategies2() {
		return allStrategies2;
	}
}
