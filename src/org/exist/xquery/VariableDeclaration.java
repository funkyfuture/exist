/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-03 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id$
 */
package org.exist.xquery;

import org.exist.dom.QName;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;

/**
 * A global variable declaration (with: declare variable). Variable bindings within
 * for and let expressions are handled by {@link org.exist.xquery.ForExpr} and
 * {@link org.exist.xquery.LetExpr}.
 * 
 * @author wolf
 */
public class VariableDeclaration extends AbstractExpression {

	String qname;
	SequenceType sequenceType = null;
	Expression expression;
	
	/**
	 * @param context
	 */
	public VariableDeclaration(XQueryContext context, String qname, Expression expr) {
		super(context);
		this.qname = qname;
		this.expression = expr;
	}

	/**
	 * Set the sequence type of the variable.
	 * 
	 * @param type
	 */
	public void setSequenceType(SequenceType type) {
		this.sequenceType = type;
	}
	
	/* (non-Javadoc)
     * @see org.exist.xquery.Expression#analyze(org.exist.xquery.Expression)
     */
    public void analyze(Expression parent, int flags) throws XPathException {
        QName qn = QName.parse(context, qname, null);
		Module myModule = context.getModule(qn.getNamespaceURI());
		if(myModule != null) {
            if (myModule.isVarDeclared(qn))
                throw new XPathException(getASTNode(), "err:XQST0049: It is a static error if more than one " +
                    "variable declared or imported by a module has the same expanded QName. Variable: " + qn);
			myModule.declareVariable(qn, null);
        } else {
            if(context.isVarDeclared(qn)) {
                throw new XPathException(getASTNode(), "err:XQST0049: It is a static error if more than one " +
                        "variable declared or imported by a module has the same expanded QName. Variable: " + qn);
            }
			Variable var = new Variable(qn);
			context.declareGlobalVariable(var);
		}
    }
    
	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.dom.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(
		Sequence contextSequence,
		Item contextItem)
		throws XPathException {
		QName qn = QName.parse(context, qname, null);
		Module myModule = context.getModule(qn.getNamespaceURI());
		
		// declare the variable
		Sequence seq = expression.eval(contextSequence, contextItem);

		if(myModule != null) {
			Variable var = myModule.declareVariable(qn, seq);
            var.setSequenceType(sequenceType);
            var.checkType();
        } else {
			Variable var = new Variable(qn);
			var.setValue(seq);
            var.setSequenceType(sequenceType);
            var.checkType();
			context.declareGlobalVariable(var);
		}
		return Sequence.EMPTY_SEQUENCE;
	}

	/* (non-Javadoc)
     * @see org.exist.xquery.Expression#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        dumper.nl().display("declare variable $").display(qname, getASTNode());
        if(sequenceType != null) {
            dumper.display(" as ").display(sequenceType.toString());
        }
        dumper.display('{');
        dumper.startIndent();
        expression.dump(dumper);
        dumper.endIndent();
        dumper.nl().display('}').nl();
    }
    
	/* (non-Javadoc)
	 * @see org.exist.xquery.Expression#returnsType()
	 */
	public int returnsType() {
		return expression.returnsType();
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#getDependencies()
	 */
	public int getDependencies() {
		return expression.getDependencies();
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#getCardinality()
	 */
	public int getCardinality() {
		return expression.getCardinality();
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#resetState()
	 */
	public void resetState() {
		expression.resetState();
	}
}
