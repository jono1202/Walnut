/*   Copyright 2022 Anatoly Zavyalov
 *
 *   This file is part of Walnut.
 *
 *   Walnut is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Walnut is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Walnut.  If not, see <http://www.gnu.org/licenses/>.
 */

package Automata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import java.lang.Math;

import Main.UtilityMethods;

/**
 * The class Transducer represents a deterministic finite-state transducer with all states final that is 1-uniform.
 * <p>
 * It is implemented by constructing a deterministic finite-state automaton with all states final, and adding on top a
 * 1-uniform output function with the domain S x A (where S is the set of states and A is the input alphabet),
 * and the codomain being an output alphabet (subset of the integers).
 *
 * @author Anatoly
 */
public class Transducer extends Automaton {

    /**
     * Output Alpabet for the output function sigma.
     * For example when G = [[-1, 1], [0, 1, 2, 3]], the first and second inputs are over the alphabets
     * {-1, 1} and {0, 1, 2, 3} respectively.
     *
     * Note that G is a list of sets, but for technical reasons, it is a list of lists. However, we must make sure that,
     * at all times, the inner lists of G do not contain repeated elements.
     */
    public List<List<Integer>> G;

    /**
     * Output alphabet size.
     * For example, if G = [[-1, 1], [0, 1, 2, 3]], then outputAlphabetSize = 8, and if G = [[-1, 0, 1], [1, 2, 3, 4]],
     * then outputAlphabetSize = 12.
     */
    public int outputAlphabetSize;

    /**
     * Output function for the Transducer.
     * For example, when sigma[0] = [(0, 1), (1, 0), (2, -1), (3, 2), (4, -1), (5, 3)]
     * and input alphabet A = [[0, 1], [-1, 2, 3]],
     * then from state 0 on
     * (0, -1) we output 1
     * (0, 2) we output 0
     * (0, 3) we output -1
     * (1, -1) we output 2
     * (1, 2) we output -1
     * (1, 3) we output 3
     *
     * Just like in an Automaton's transition function d, we store the encoded values of inputs in sigma, so instead of
     * saying that "on (0, -1) we output 1", we really store "on 0, output 1".
     */
    public List<TreeMap<Integer, Integer>> sigma;

    /**
     * Default constructor for Transducer. Calls the default constructor for Automaton.
      */
    public Transducer() {
        super();

        G = new ArrayList<List<Integer>>();

        outputAlphabetSize = 0;

        sigma = new ArrayList<TreeMap<Integer, Integer>>();
    }

    /**
     * Takes an address and constructs the transducer represented by the file referred to by the address.
     * @param address
     * @throws Exception
     */
    public Transducer(String address) throws Exception {
        this();
        final String REGEXP_FOR_WHITESPACE = "^\\s*$";

        // lineNumber will be used in error messages
        int lineNumber = 0;

        alphabetSize = 1;

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(address), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                lineNumber++;

                if (line.matches(REGEXP_FOR_WHITESPACE)) {
                    // Ignore blank lines.
                    continue;
                }
                else {
                    boolean flag = false;
                    try {
                        flag = ParseMethods.parseAlphabetDeclaration(line, A, NS);

                    } catch (Exception e) {
                        in.close();
                        throw new Exception(
                                e.getMessage() + UtilityMethods.newLine() +
                                        "\t:line "+ lineNumber + " of file " + address);
                    }

                    if (flag) {
                        for (int i = 0; i < A.size(); i++) {
                            if(NS.get(i) != null &&
                                    (!A.get(i).contains(0) || !A.get(i).contains(1))) {
                                in.close();
                                throw new Exception(
                                        "The " + (i + 1) + "th input of type arithmetic " +
                                                "of the automaton declared in file " + address +
                                                " requires 0 and 1 in its input alphabet: line " +
                                                lineNumber);
                            }
                            UtilityMethods.removeDuplicates(A.get(i));
                            alphabetSize *= A.get(i).size();
                        }

                        break;
                    }
                    else {
                        in.close();
                        throw new Exception(
                            "Undefined statement: line " +
                            lineNumber + " of file " + address);
                    }
                }
            }

            int[] pair = new int[2];
            List<Integer> input = new ArrayList<Integer>();
            List<Integer> dest = new ArrayList<Integer>();
            List<Integer> output = new ArrayList<Integer>();
            int currentState = -1;
            int currentStateOutput;
            TreeMap<Integer,List<Integer>> currentStateTransitions = new TreeMap<>();
            TreeMap<Integer, Integer> currentStateTransitionOutputs = new TreeMap<>();
            TreeMap<Integer,Integer> state_output = new TreeMap<Integer,Integer>();
            TreeMap<Integer,TreeMap<Integer,List<Integer>>> state_transition =
                    new TreeMap<Integer,TreeMap<Integer,List<Integer>>>();
            TreeMap<Integer, TreeMap<Integer, Integer>> state_transition_output =
                    new TreeMap<Integer, TreeMap<Integer, Integer>>();
            /**
             * This will hold all states that are destination of some transition.
             * Then we make sure all these states are declared.
             */
            Set<Integer> setOfDestinationStates = new HashSet<Integer>();
            Q = 0;
            while((line = in.readLine())!= null) {
                lineNumber++;
                if(line.matches(REGEXP_FOR_WHITESPACE)) {

                    continue;
                }

                if(ParseMethods.parseStateDeclaration(line, pair)) {
                    Q++;
                    if(currentState == -1) {
                        q0 = pair[0];
                    }

                    currentState = pair[0];
                    currentStateOutput = pair[1];
                    state_output.put(currentState, currentStateOutput);
                    currentStateTransitions = new TreeMap<>();
                    state_transition.put(currentState, currentStateTransitions);
                    currentStateTransitionOutputs = new TreeMap<>();
                    state_transition_output.put(currentState, currentStateTransitionOutputs);
                } else if(ParseMethods.parseTransducerTransition(line, input, dest, output)) {
                    setOfDestinationStates.addAll(dest);

                    if(currentState == -1){
                        in.close();
                        throw new Exception(
                                "Must declare a state before declaring a list of transitions: line " +
                                        lineNumber + " of file " + address);
                    }

                    if(input.size() != A.size()) {
                        in.close();
                        throw new Exception("This automaton requires a " + A.size() +
                                "-tuple as input: line " + lineNumber + " of file " + address);
                    }
                    List<List<Integer>> inputs = expandWildcard(input);

                    for(List<Integer> i : inputs) {
                        currentStateTransitions.put(encode(i), dest);
                        if (output.size() == 1) {
                            currentStateTransitionOutputs.put(encode(i), output.get(0));
                        }
                        else {
                            in.close();
                            throw new Exception("Transducers must have one output for each transition: line "
                                    + lineNumber + " of file " + address);
                        }
                    }

                    input = new ArrayList<Integer>();
                    dest = new ArrayList<Integer>();
                    output = new ArrayList<Integer>();
                }
                else{
                    in.close();
                    throw new Exception("Undefined statement: line "+ lineNumber + " of file " + address);
                }
            }
            in.close();
            for(int q:setOfDestinationStates) {
                if(!state_output.containsKey(q)) {
                    throw new Exception(
                            "State " + q + " is used but never declared anywhere in file: " + address);
                }
            }

            for(int q = 0; q < Q; q++) {
                O.add(state_output.get(q));
                d.add(state_transition.get(q));
                sigma.add(state_transition_output.get(q));
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("File does not exist: " + address);
        }
    }


    /**
     * Transduce an Automaton M as in Dekking (1994).
     *
     * Right now, assumed that the initial state of the new automaton is q0 = 0.
     * @param M -
     * @return The transduced Automaton after applying this Transducer to M.
     * @throws Exception
     */
    public Automaton transduce(Automaton M) throws Exception {
        /**
         * N will be the returned Automaton, just have to build it up.
         */
        Automaton N = new Automaton();

        // build up the automaton.
        for (int i = 0; i < M.A.size(); i++) {
            N.A.add(M.A.get(i));
            N.NS.add(M.NS.get(i));

            // Copy the encoder
            if (M.encoder != null && M.encoder.size() > 0) {
                if (N.encoder == null) {
                    N.encoder = new ArrayList<Integer>();
                }
                N.encoder.add(M.encoder.get(i));
            }

            // Copy the label
            if (M.label != null && M.label.size() == M.A.size()) {
                N.label.add(M.label.get(i));
            }
        }

        // TODO: This probably should be configurable, and should not necessarily be 0.
        N.q0 = 0;

        /*
            Need to find P and Q so the transition function of the Transducer becomes ultimately periodic with lag Q
            and period P.
         */

        // periods of the sequence (phi_s, phi_{h(s)}, phi_{h^2(s)}, ...) for each state s where h is the underlying
        // morphism of h
        ArrayList<Integer> pPerState = new ArrayList<Integer>();
        ArrayList<Integer> qPerState = new ArrayList<Integer>();

        for (int z = 0; z < M.Q; z++) {
            // this will contain (z, h(z), h^2(z), ..., h^m(z)) for some m.
            ArrayList<List<Integer>> hIterates = new ArrayList<List<Integer>>();

            // hIterates[0] should just be [z] alone.
            hIterates.add(Arrays.asList(z));


            // >= 0 if m is p+q-1, if this is -1 then no m has been found yet.
            // Set to -1 by default but will be whatever m is once an m is found.
            int mFound = -1, nFound = -1;

            // m will be the upper index. Note that m can be as large as needed.
            for (int m = 1; ; m++) {
                // hIterates should always have an element at index m-1, as a new one is added just before the loop
                // and a new one is added in each iteration of this loop.

                // this will be h^m(z)
                ArrayList<Integer> mthPower = new ArrayList<Integer>();

                // for every "character" in h^{m-1}(z), add new
                for (int x : hIterates.get(m - 1)) {

                    // for every digit in the alphabet of M
                    for (int l : M.d.get(x).keySet()) {

                        // each list of states that this transition goes to.
                        // we assuming it's a DFA for now, so this has length 1 we're assuming...

                        // get the first index of M.d on state x and edge label l
                        mthPower.add(M.d.get(x).get(l).get(0));

                    }
                }

                // add h^m(z)
                hIterates.add(mthPower);

                // now need to compare phi_{h^m(z)} with phi_{h^n(z)},
                // where phi is the transition function of the transducer.
                for (int n = 0; n < m; n++) {

                    // >= 0 if n is q, if this is -1 then no n has been found yet where phi_{h^m(z)} = phi_{h^n(z)}.
                    // Set to -1 by default but will be whatever n is once an n is found.
                    nFound = n;

                    // iterate through the states of the transducer.
                    // need phi_{h^m(z)} and phi_{h^n(z)} to match on all states s of the transducer
                    for (int s = 0; s < Q; s++) {

                        /**
                         * We know that h^m(z) = [x0, ..., x{k-1}] for some k, so
                         *  phi_{h^m(z)} = phi_{x_k} o phi_{x_{k-1}} o ... o phi_{x_1}, where o denotes composition.
                         */

                        int mState = s;

                        for (int i = 0; i < hIterates.get(m).size(); i++) {
                            /**
                             * This will be an incremental value,
                             * phi_{x_{i}} o phi_{x_{i-1}} o ... o phi_{x_0}(s)
                             * = phi_{x_{i}} (phi_{x_{i-1}} o ... o phi_{x_0}(s))
                             */
                            mState = d.get(mState).get(hIterates.get(m).get(i)).get(0);
                        }

                        // similarly for n.

                        int nState = s;

                        for (int i = 0; i < hIterates.get(n).size(); i++) {
                            nState = d.get(nState).get(hIterates.get(n).get(i)).get(0);
                        }

                        // if the n state is not equal to the m state, break.
                        if (mState != nState) {
                            nFound = -1;

                            break;
                        }
                    }
                    if (nFound >= 0) {
                        mFound = m;
                        break;
                    }
                }

                if (mFound >= 0) {
                    break;
                }
            }

            // now a period p and lag q have been found for this initial state, namely we have
            // this p being m-n and q being n. so
            pPerState.add(mFound - nFound);
            qPerState.add(nFound);
        }

        // now need p and q.

        // make q by getting the max.
        int q = 0;
        for (int i = 0; i < qPerState.size(); i++) {
            if (q < qPerState.get(i)) {
                q = qPerState.get(i);
            }
        }
        // q should be max{qPerState[0], ..., qPerState[qPerState.length - 1]}

        // make p by getting the LCM of all of the values of p for each individual sequence.
        int p = UtilityMethods.lcmOfList(pPerState);

        /*
            Make the states of the automaton.
         */

        // hashmap of Map to String (to keep track of all maps)
        HashMap<Map<Integer, Integer>, List<Integer>> mapsToString = new HashMap<Map<Integer, Integer>,List<Integer>>();

        // start with the empty string.
        HashMap<Integer, Integer> identity = new HashMap<Integer, Integer>();

        class MapStringTuple {
            final Map<Integer, Integer> map;
            final List<Integer> string;
            MapStringTuple(Map<Integer, Integer> map, List<Integer> string) {
                this.map = map;
                this.string = string;
            }
        }

        // queue of maps TUPLES because you also need the string associated with it.
        Queue<MapStringTuple> queue = new LinkedList<>();

        // identity will be the identity, so we want to iterate through the states of the transducer.
        for (int i = 0; i < Q; i++) {
            identity.put(i, i);
        }

        // put the identity.
        mapsToString.put(identity, Arrays.asList());

        // add (id, []) to queue
        queue.add(new MapStringTuple(identity, Arrays.asList()));

        while (queue.size() > 0) {

            MapStringTuple tuple = queue.remove();

            // iterate through the states of the automaton to be transduced
            for (int i = 0; i < M.Q; i++) {
                HashMap<Integer, Integer> newMap = new HashMap<Integer, Integer>();

                for (int j = 0; j < Q; j++) {
                    // construct phi_{O(w x_i)}, where oldMap = phi_{O(w)}
                    newMap.put(j, d.get(tuple.map.get(j)).get(M.O.get(i)).get(0));
                }
                if (!mapsToString.containsKey(newMap)) {
                    List<Integer> newString = new ArrayList<>(tuple.string);
                    newString.add(i); // add the string BEFORE the projection from the automaton M.

                    mapsToString.put(newMap, newString);

                    queue.add(new MapStringTuple(newMap, newString));
                }
            }
        }

        // now to generate the actual states.

        // tuple of the form (a, iters) where iters is a list of p+q maps phi_{M.O(w)}, ..., phi_{h^{p+q-1}(M.O(W))}
        class StateIteratesTuple {
            final int state;
            final List<Map<Integer, Integer>> iterates;
            StateIteratesTuple(int state, List<Map<Integer, Integer>> iterates) {
                this.state = state;
                this.iterates = iterates;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                StateIteratesTuple other = (StateIteratesTuple) o;
                if (this.state != other.state) {
                    return false;
                }
                if (this.iterates != other.iterates) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int result = (int) (this.state ^ (this.state >>> 32));
                result = 31 * result + this.iterates.hashCode();
                return result;
            }
        }

        ArrayList<StateIteratesTuple> states = new ArrayList<StateIteratesTuple>();

        HashMap<StateIteratesTuple, Integer> statesHash = new HashMap<StateIteratesTuple, Integer>();

        // this is a map phi_{M.O(w)} -> [phi_{M.O(w)}, phi_{M.O(h(w))}, ..., phi_{M.O(h^{p+q-1}(w))}]
        HashMap<Map<Integer, Integer>, List<Map<Integer, Integer>>> mapToHIterates = new HashMap<Map<Integer, Integer>, List<Map<Integer, Integer>>>();

        // fill up mapToHIterates
        for (Map<Integer, Integer> map : mapsToString.keySet()) {
            // this will be [w, h(w), ..., h^{p+q-1}(w)]
            ArrayList<List<Integer>> hIterateStrings = new ArrayList<List<Integer>>();

            // this will be [phi_{M.O(w)}, ... phi_{M.O(h^{p+q-1}(w))}]
            ArrayList<Map<Integer, Integer>> hIterateMaps = new ArrayList<Map<Integer, Integer>>();

            // add w
            hIterateStrings.add(mapsToString.get(map));

            // add phi_{M.O(w)}
            hIterateMaps.add(map);

            for (int j = 1; j < p + q; j++) {
                List<Integer> hIterateStringPrev = hIterateStrings.get(hIterateStrings.size() - 1);
                List<Integer> hIterateStringNew = new ArrayList<Integer>();
                for (int u = 0; u < hIterateStringPrev.size(); u++) {

                    // for every digit in the alphabet of M
                    for (int l : M.d.get(u).keySet()) {

                        // each list of states that this transition goes to.
                        // we assuming it's a DFA for now, so this has length 1 we're assuming...

                        // get the first index of M.d on state x and edge label l

                        hIterateStringNew.add(M.d.get(hIterateStringPrev.get(u)).get(l).get(0));
                    }
                }

                hIterateStrings.add(hIterateStringNew);

                ArrayList<Integer> projectedString = new ArrayList<Integer>();
                for (int u = 0; u < hIterateStringNew.size(); u++) {
                    projectedString.add(M.O.get(hIterateStringNew.get(u)));
                }
                // now need to define the phi_{projectedString}. Will do this iteratively.

                // start off with the identity.
                HashMap<Integer, Integer> mapSoFar = identity;

                for (int u = 0; u < projectedString.size(); u++) {
                    HashMap<Integer, Integer> newMap = new HashMap<Integer, Integer>();
                    for (int l = 0; l < Q; l++) {
                        newMap.put(l, d.get(mapSoFar.get(l)).get(projectedString.get(u)).get(0));
                    }
                    mapSoFar = newMap;
                }

                hIterateMaps.add(mapSoFar);
            }

            mapToHIterates.put(map, hIterateMaps);
        }

        for (int i = 0; i < M.Q; i++) {
            for (Map<Integer, Integer> map : mapsToString.keySet()) {

                // put the tuple into the states hash and list.
                StateIteratesTuple tuple = new StateIteratesTuple(i, mapToHIterates.get(map));

                statesHash.put(tuple, states.size());

                states.add(tuple);

            }
        }


        N.Q = states.size();



        // the initial state is (M.q0, hIterates(identity)) where
        // hIterates(identity) is the hIterates corresponding to the identity map
        StateIteratesTuple initialState = new StateIteratesTuple(M.q0, mapToHIterates.get(identity));

        N.q0 = statesHash.get(initialState);


        /*
            Implement the transition function of this new automaton.
         */
        for (int i = 0; i < N.Q; i++) {
            
            N.d.add(new TreeMap<Integer,List<Integer>>());
            for (int l : M.d.get(states.get(i).state).keySet()) {

                // apply the transition function to m
                // the state in M to use as the first element in the tuple
                int stateM = M.d.get(states.get(i).state).get(l).get(0);

                // figure out the string to find the hIterates of (we do this by finding its corresponding map)
                ArrayList<Integer> secondCoordString = new ArrayList<Integer>();

                // w associated with the hIterates [phi_{M.O(w)}, ...]
                // get the string by looking at the first map in hIterates then use the map to string hashmap
                List<Integer> stateString = mapsToString.get(states.get(i).iterates.get(0));

                for (int u = 0; u < stateString.size(); u++) {

                    // for every digit in the alphabet of M
                    for (int y : M.d.get(stateString.get(u)).keySet()) {

                        // each list of states that this transition goes to.
                        // we assuming it's a DFA for now, so this has length 1 we're assuming...

                        // get the first index of M.d on state x and edge label y
                        secondCoordString.add(M.O.get(M.d.get(stateString.get(u)).get(y).get(0)));
                    }
                }

                // add M.O(sigma(a)_1), ..., M.O(sigma(a)_{l-1})
                for (int u = 0; u < l; u++) {
                    secondCoordString.add(M.O.get(M.d.get(states.get(i).state).get(u).get(0)));
                }

                // now create the map associated with this iteratively, like before
                // start off with the identity.
                HashMap<Integer, Integer> mapSoFar = identity;

                for (int u = 0; u < secondCoordString.size(); u++) {
                    HashMap<Integer, Integer> newMap = new HashMap<Integer, Integer>();
                    for (int y = 0; y < Q; y++) {
                        newMap.put(y, d.get(mapSoFar.get(y)).get(secondCoordString.get(u)).get(0));
                    }
                    mapSoFar = newMap;
                }

                // now mapSoFar is a map that has hIterates associated with it.

                N.d.get(i).put(l, Arrays.asList(statesHash.get(new StateIteratesTuple(stateM, mapToHIterates.get(mapSoFar)))));
            }
        }

        /*
            Implement the projections (output for each state) to be
            h(a, hIterates) = sigma(d(q, M.O(w)), a).
        */

        for (int i = 0; i < N.Q; i++) {

            Map<Integer, Integer> map = states.get(i).iterates.get(0);

            // map.get(N.q0) will be the current state.

            // set what N.O.get(i) will be
            N.O.add(sigma.get(map.get(N.q0)).get(states.get(i).state));
        }

        N.alphabetSize = M.alphabetSize;

        return N;
    }
}