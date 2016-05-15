# election

[![Build Status](https://travis-ci.org/waitingcheung/election.svg?branch=master)](https://travis-ci.org/waitingcheung/election)
[![codecov](https://codecov.io/gh/waitingcheung/election/branch/master/graph/badge.svg)](https://codecov.io/gh/waitingcheung/election)

This program computes all pure Nash equilibria of a 2-party election game described in the paper:

Ning Ding, Fangzhen Lin: [On Computing Optimal Strategies in Open List
Proportional Representation: The Two Parties Case]. AAAI 2014:
1419-1425.

### Input
- C1, a set of candidates in party 1.
- C2, a set of candidates in party 2.
- S, number of seats.
- f: a counting function on the union of C1 and C2. f(c) is the number
 of votes that the candidate c will attract.

### Output
All possible strategy pairs that are Nash equilibria.

### Input Format
The input will be a text file with three lines:
- line 1: S
- line 2: C1 and f on C1 in pairs seperated by comma, like (c1,2),
(c2,5), ...
- line 3: similarly for party 2.

This program will write to the standard output Nash equilibria with
their corresponding utility profiles, like:

```sh
({{c1,c2},{c3}},{{c5},{c6}}) payoff (2 1/2, 1/2)
({{c1},{c2,c3}},{{c5,c6}}) payoff (2 1/2, 1/2)
...
```

### Compilation

```sh
mvn compile
```

### Execution

```sh
./hklc file
```

For example, running "./hklc examples/exampleX" will produce the outputs for the generated test files. The file hklc is a shell script that calls the java classes. In case there is permission problem in running hklc please run "chmod +x ./hklc".

### Heuristics

This program reduces the search space by eliminating strategies of one party that do not produce the highest payoffs for each known strategy of another party. Then it validates in the reduced search space if each pair of strategies is nash equilibrium. Before comparing each payoff in validation, it uses a heurisitc that sorts the payoffs of all lists in both parties and combines the remaining votes of a party to see if the combined lists can make them a higher ranking in the sorted list. Such heuristic reduces the likelihood of exhausive comparisions of all payoffs in the reduced search space as for each pair of strategies there are at most 20 lists to sort for 10x10 input size. To reduce the need for repeated computations, it stores most of the intermediate results in hashmaps for quick lookup.

[On Computing Optimal Strategies in Open List Proportional Representation: The Two Parties Case]: http://www.aaai.org/ocs/index.php/AAAI/AAAI14/paper/view/8452
