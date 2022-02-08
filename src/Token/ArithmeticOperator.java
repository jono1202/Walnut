/*	 Copyright 2016 Hamoon Mousavi
 * 
 * 	 This file is part of Walnut.
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

package Token;
import java.util.Stack;

import Main.Expression;
import Automata.Automaton;
import Automata.NumberSystem;
import Main.Type;
import Main.UtilityMethods;


public class ArithmeticOperator extends Operator{
	NumberSystem number_system;
	public ArithmeticOperator(int position,String op, NumberSystem number_system) throws Exception{
		this.op = op;
		setPriority();
		if (op.equals("_")) {
			setArity(1);
		} else {
			setArity(2);
		}
		setPositionInPredicate(position);
		this.number_system = number_system;
	}
	public String toString(){
		return op+"_"+number_system;
	}
	public void act(Stack<Expression> S,boolean print,String prefix,StringBuffer log) throws Exception{
		if(S.size() < getArity())throw new Exception("operator " + op + " requires " + getArity()+ " operands");
		Expression b = S.pop();
		if(!(b.is(Type.arithmetic) || b.is(Type.variable) || b.is(Type.numberLiteral)))
			throw new Exception("operator " + op + " cannot be applied to the operand " +b+" of type " + b.getType());

		if(op.equals("_")) {
			if(b.is(Type.numberLiteral)) {
				S.push(new Expression(Integer.toString(-b.constant), -b.constant, number_system));
				return;
			}
			String c = getUniqueString();
			// b + c = 0
			Automaton M = number_system.arithmetic(b.identifier,c,0,"+");
			String preStep = prefix + "computing " + op+b;
			log.append(preStep + UtilityMethods.newLine());
			if(print){
				System.out.println(preStep);
			}
			if(b.is(Type.arithmetic)){
				// Eb, b + c = 0 & M(b,...)
				M = M.and(b.M,print,prefix+" ",log);
				M.quantify(b.identifier,print,prefix+" ",log);
			}
			S.push(new Expression("("+op+b+")",M,c));
			String postStep = prefix + "computed " + op+b;
			log.append(postStep + UtilityMethods.newLine());
			if(print){
				System.out.println(postStep);
			}
			return;
		}

		Expression a = S.pop();
		if(!(a.is(Type.arithmetic) || a.is(Type.variable) || a.is(Type.numberLiteral)))
			throw new Exception("operator " + op + " cannot be applied to the operand "+ a+ " of type " + a.getType());

		if(a.is(Type.numberLiteral) && b.is(Type.numberLiteral)){
			switch(op){
				case "+":
					S.push(new Expression(Integer.toString(a.constant+b.constant),a.constant+b.constant,number_system));
					return;
				case "*":
					S.push(new Expression(Integer.toString(a.constant*b.constant),a.constant*b.constant,number_system));
					return;
				case "/":
					S.push(new Expression(Integer.toString(a.constant/b.constant),a.constant/b.constant,number_system));
					return;
				case "-":
					S.push(new Expression(Integer.toString(a.constant-b.constant),a.constant-b.constant,number_system));
					return;
			}
		}
		String c = getUniqueString();
		Automaton M;
		if(a.is(Type.numberLiteral)){
			if(a.constant == 0 && op.equals("*")){
				S.push(new Expression("0",0,number_system));
				return;
			}
			else
				M = number_system.arithmetic(a.constant, b.identifier, c, op);
		}
		else if(b.is(Type.numberLiteral)){
			if(b.constant == 0 && op.equals("*")){
				S.push(new Expression("0",0,number_system));
				return;
			}
			M = number_system.arithmetic(a.identifier, b.constant, c, op);
		}
		else{
			M = number_system.arithmetic(a.identifier, b.identifier, c, op);
		}
		String preStep = prefix + "computing " + a+op+b;
		log.append(preStep + UtilityMethods.newLine());
		if(print){
			System.out.println(preStep);
		}
		if(a.is(Type.arithmetic)){
			M = M.and(a.M,print,prefix+" ",log);
			M.quantify(a.identifier,print,prefix+" ",log);
		}
		if(b.is(Type.arithmetic)){
			M = M.and(b.M,print,prefix+" ",log);
			M.quantify(b.identifier,print,prefix+" ",log);
		}
		S.push(new Expression("("+a+op+b+")",M,c));
		String postStep = prefix + "computed " + a+op+b;
		log.append(postStep + UtilityMethods.newLine());
		if(print){
			System.out.println(postStep);
		}
	}
}
