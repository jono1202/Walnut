///*   Copyright 2022 Anatoly Zavyalov
// *
// *   This file is part of Walnut.
// *
// *   Walnut is free software: you can redistribute it and/or modify
// *   it under the terms of the GNU General Public License as published by
// *   the Free Software Foundation, either version 3 of the License, or
// *   (at your option) any later version.
// *
// *   Walnut is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *   GNU General Public License for more details.
// *
// *   You should have received a copy of the GNU General Public License
// *   along with Walnut.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package Automata;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.TreeMap;
//
//import java.lang.Math;
//
//import Main.UtilityMethods;
//
///**
// * The class Transducer represents a deterministic finite-state transducer with all states final that is 1-uniform.
// * <p>
// * It is implemented by constructing a deterministic finite-state automaton with all states final, and adding on top a
// * 1-uniform output function with the domain S x A (where S is the set of states and A is the input alphabet),
// * and the codomain being an output alphabet (subset of the integers).
// *
// * @author Anatoly
// */
//public class Transducer extends Automaton {
//
//    /**
//     * Output Alpabet for the output function sigma.
//     * For example when G = [[-1, 1], [0, 1, 2, 3]], the first and second inputs are over the alphabets
//     * {-1, 1} and {0, 1, 2, 3} respectively.
//     *
//     * Note that G is a list of sets, but for technical reasons, it is a list of lists. However, we must make sure that,
//     * at all times, the inner lists of G do not contain repeated elements.
//     */
//    public List<List<Integer>> G;
//
//    /**
//     * Output alphabet size.
//     * For example, if G = [[-1, 1], [0, 1, 2, 3]], then outputAlphabetSize = 8, and if G = [[-1, 0, 1], [1, 2, 3, 4]],
//     * then outputAlphabetSize = 12.
//     */
//    public int outputAlphabetSize;
//
//    /**
//     * Output function for the Transducer.
//     * For example, when sigma[0] = [(0, [-1, 1]), (1, [0, 0]), (2, [-1, 0]), (3, [1, 1]), (4, [-1, 0]), (5, [-1, 1])]
//     * and input alphabet A = [[0, 1], [-1, 2, 3]] and output alphabet G = [[-1, 0, 1], [0, 1]],
//     * then from state 0 on
//     * (0, -1) we output (-1, 1)
//     * (0, 2) we output (0, 0)
//     * (0, 3) we output (-1, 0)
//     * (1, -1) we output (1, 1)
//     * (1, 2) we output (-1, 0)
//     * (1, 3) we output (-1, 1)
//     *
//     * Just like in an Automaton's transition function d, we store the encoded values of inputs in sigma, so instead of
//     * saying that "on (0, -1) we output (-1, 1)", we really store "on 0, output (-1, 1)".
//     */
//    public List<TreeMap<Integer, List<Integer>>> sigma;
//
//    /**
//     * Default constructor for Transducer. Calls the default constructor for Automaton.
//      */
//    public Transducer() {
//        super();
//
//        G = new ArrayList<List<Integer>>();
//        outputAlphabetSize = 0;
//
//        sigma = new ArrayList<TreeMap<Integer, List<Integer>>>();
//    }
//
//    /**
//     * TODO: Need a constructor to read from a .txt file.
//     */
//
//    /**
//     * Transduce an Automaton M as in Dekking (1994).
//     *
//     * Right now, assumed that the initial state of the new automaton is q0 = 0.
//     * @param M -
//     * @return The transduced Automaton after applying this Transducer to M.
//     * @throws Exception
//     */
//    public Automaton transduce(Automaton M) throws Exception {
//        /**
//         * N will be the returned Automaton, just have to build it up.
//         */
//        Automaton N = new Automaton();
//
//        // build up the automaton.
//        for (int i = 0; i < M.A.size(); i++) {
//            N.A.add(M.A.get(i));
//            N.NS.add(M.NS.get(i));
//
//            // Copy the encoder
//            if (M.encoder != null && M.encoder.size() > 0) {
//                if (N.encoder == null) {
//                    N.encoder = new ArrayList<Integer>();
//                }
//                N.encoder.add(M.encoder.get(i));
//            }
//
//            // Copy the label
//            if (M.label != null && M.label.size() == M.A.size()) {
//                N.label.add(M.label.get(i));
//            }
//        }
//
//        // TODO: This probably should be configurable, and should not necessarily be 0.
//        N.q0 = 0;
//
//        /*
//            Need to find P and Q so the transition function of the Transducer becomes ultimately periodic with lag Q
//            and period P.
//         */
//
//        // periods of the sequence (phi_s, phi_{h(s)}, phi_{h^2(s)}, ...) for each state s where h is the underlying
//        // morphism of h
//        int[] pPerState = new int[M.Q];
//
//        // lags of the sequence for each state
//        int[] qPerState = new int[M.Q];
//
//        for (int z = 0; z < M.Q; z++) {
//            // this will contain (z, h(z), h^2(z), ..., h^m(z)) for some m.
//            ArrayList<List<Integer>> hIterates = new ArrayList<List<Integer>>();
//
//            // hIterates[0] should just be [z] alone.
//            hIterates.add(Arrays.asList(z));
//
//            // m will be the upper index. Note that m can be as large as needed.
//            for (int m = 1; ; m++) {
//                // hIterates should always have an element at index m-1, as a new one is added just before the loop
//                // and a new one is added in each iteration of this loop.
//
//                // this will be h^m(z)
//                ArrayList<Integer> mthPower = new ArrayList<Integer>();
//
//                // for every "character" in h^{m-1}(z), add new
//                for (int x : hIterates.get(m - 1)) {
//
//                    // for every digit in the alphabet of M
//                    for (int l : M.d.get(x).keySet()) {
//
//                        // each list of states that this transition goes to.
//                        // we assuming it's a DFA for now, so this has length 1 we're assuming...
//
//                        // get the first index of M.d on state x and edge label l
//                        mthPower.add(M.d.get(x).get(l).get(0));
//
//                    }
//                }
//
//                // add h^m(z)
//                hIterates.add(mthPower);
//
//                // >= 0 if m is p+q-1, if this is -1 then no m has been found yet.
//                // Set to -1 by default but will be whatever m is once an m is found.
//                int mFound = -1;
//
//                // now need to compare phi_{h^m(z)} with phi_{h^n(z)},
//                // where phi is the transition function of the transducer.
//                for (int n = 0; n < m; n++) {
//
//                    // >= 0 if n is q, if this is -1 then no n has been found yet where phi_{h^m(z)} = phi_{h^n(z)}.
//                    // Set to -1 by default but will be whatever n is once an n is found.
//                    int nFound = n;
//
//                    // iterate throuhg the states of the transducer.
//                    // need phi_{h^m(z)} and phi_{h^n(z)} to match on all states s of the transducer
//                    for (int s = 0; s < Q; s++) {
//
//                        /**
//                         * We know that h^m(z) = [x0, ..., x{k-1}] for some k, so
//                         *  phi_{h^m(z)} = phi_{x_k} o phi_{x_{k-1}} o ... o phi_{x_1}, where o denotes composition.
//                         */
//
//                        int mState = s;
//
//                        for (int i = 0; i < hIterates.get(m).size(); i++) {
//                            /**
//                             * This will be an incremental value,
//                             * phi_{x_{i}} o phi_{x_{i-1}} o ... o phi_{x_0}(s)
//                             * = phi_{x_{i}} (phi_{x_{i-1}} o ... o phi_{x_0}(s))
//                             */
//                            mState = d.get(mState).get(hIterates.get(m).get(i));
//                        }
//
//                        // similarly for n.
//
//                        int nState = s;
//
//                        for (int i = 0; i < hIterates.get(n).size(); i++) {
//                            nState = d.get(nState).get(hIterates.get(n).get(i));
//                        }
//
//                        // if the n state is not equal to the m state, break.
//                        if (mState != nState) {
//                            nFound = -1;
//
//                            break;
//                        }
//                    }
//                    if (nFound >= 0) {
//                        mFound = m;
//                        break;
//                    }
//                }
//
//                if (mFound >= 0) {
//                    break;
//                }
//            }
//
//            // now a period p and lag q have been found for this initial state, namely we have
//            // this p being m-n and q being n. so
//            pPerState[z] = mFound - nFound;
//            qPerState[z] = nFound;
//        }
//
//        // now need p and q.
//
//        // make q by getting the max.
//        int q = 0;
//        for (int i = 0; i < qPerState.length; i++) {
//            if (q < qPerState[i]) {
//                q = qPerState[i];
//            }
//        }
//        // q should be max{qPerState[0], ..., qPerState[qPerState.length - 1]}
//
//        // make p by getting the LCM of all of the values of p for each individual sequence.
//        int p = UtilityMethods.lcmOfList(new ArrayList<>(Arrays.asList(pPerState)));
//
//        // now we have our p and q!!!
//
//        /*
//            Make the states of this new Automaton N to be
//            {(a, f_1, f_2, ..., f_{P+Q}) : a \in M.A and f_i \in Q^Q }.
//            A lot of these states wouldn't actually be reached, so need to implement BFS for this.
//         */
//
//        // this is extremely extremely bad, as it creates |M.A| * (|Q|^|Q|)^{P+Q} states, when there nearly should not be this many.
//        N.Q = M.A.size() * Math.pow(Math.pow(Q, Q), p + q);
//
//        /*
//            Need a way of taking each function from the possible states and enumerating them.
//            We will assign a natural ordering...
//         */
//
//
//        /*
//            Implement the transition function of this new automaton to be
//            d((a, f_1, f_2, ..., f_{P+Q}), d_i ) = (M.d(a, d_i), f_1, ..., f_{P+Q}).
//         */
//
//        /*
//            Implement the projections (output for each state) to be
//            h(a, f_1, f_2, ..., f_{P+Q}) = sigma(d(q, w), a).
//         */
//
//        N.alphabetSize = alphabetSize;
//    }
//}