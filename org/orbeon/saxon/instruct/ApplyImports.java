package org.orbeon.saxon.instruct;
import org.orbeon.saxon.Configuration;
import org.orbeon.saxon.Controller;
import org.orbeon.saxon.expr.*;
import org.orbeon.saxon.om.Item;
import org.orbeon.saxon.om.NodeInfo;
import org.orbeon.saxon.style.StandardNames;
import org.orbeon.saxon.trans.DynamicError;
import org.orbeon.saxon.trans.Mode;
import org.orbeon.saxon.trans.XPathException;
import org.orbeon.saxon.trans.Rule;
import org.orbeon.saxon.type.ItemType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
* An xsl:apply-imports element in the stylesheet
*/

public class ApplyImports extends Instruction {

    WithParam[] actualParams = null;
    WithParam[] tunnelParams = null;
    private boolean backwardsCompatible;

    public ApplyImports(boolean backwardsCompatible) {
        this.backwardsCompatible = backwardsCompatible;
    }

    /**
     * Set the actual parameters on the call
     */

    public void setActualParameters(
                        WithParam[] actualParams,
                        WithParam[] tunnelParams ) {
        this.actualParams = actualParams;
        this.tunnelParams = tunnelParams;
    }

    /**
    * Get the name of this instruction for diagnostic and tracing purposes
    */
    public int getInstructionNameCode() {
        return StandardNames.XSL_APPLY_IMPORTS;
    }

    /**
     * Simplify an expression. This performs any static optimization (by rewriting the expression
     * as a different expression).
     *
     * @exception org.orbeon.saxon.trans.XPathException if an error is discovered during expression
     *     rewriting
     * @return the simplified expression
     */

    public Expression simplify(StaticContext env) throws XPathException {
        WithParam.simplify(actualParams, env);
        WithParam.simplify(tunnelParams, env);
        return this;
    }

    public Expression typeCheck(StaticContext env, ItemType contextItemType) throws XPathException {
        WithParam.typeCheck(actualParams, env, contextItemType);
        WithParam.typeCheck(tunnelParams, env, contextItemType);
        return this;
    }

    public Expression optimize(Optimizer opt, StaticContext env, ItemType contextItemType) throws XPathException {
        WithParam.optimize(opt, actualParams, env, contextItemType);
        WithParam.optimize(opt, tunnelParams, env, contextItemType);
        return this;
    }

    /**
     * Determine whether this instruction creates new nodes.
     * This implementation returns true (which is almost invariably the case, so it's not worth
     * doing any further analysis to find out more precisely).
     */

    public final boolean createsNewNodes() {
        return true;
    }

    /**
     * Handle promotion offers, that is, non-local tree rewrites.
     * @param offer The type of rewrite being offered
     * @throws XPathException
     */

    protected void promoteInst(PromotionOffer offer) throws XPathException {
        WithParam.promoteParams(actualParams, offer);
        WithParam.promoteParams(tunnelParams, offer);
    }

    /**
     * Get all the XPath expressions associated with this instruction
     * (in XSLT terms, the expression present on attributes of the instruction,
     * as distinct from the child instructions in a sequence construction)
     */

    public Iterator iterateSubExpressions() {
        ArrayList list = new ArrayList(10);
        WithParam.getXPathExpressions(actualParams, list);
        WithParam.getXPathExpressions(tunnelParams, list);
        return list.iterator();
    }

    /**
     * Replace one subexpression by a replacement subexpression
     * @param original the original subexpression
     * @param replacement the replacement subexpression
     * @return true if the original subexpression is found
     */

    public boolean replaceSubExpression(Expression original, Expression replacement) {
        boolean found = false;
        if (WithParam.replaceXPathExpression(actualParams, original, replacement)) {
            found = true;
        }
        if (WithParam.replaceXPathExpression(tunnelParams, original, replacement)) {
            found = true;
        }
                return found;
    }


    public TailCall processLeavingTail(XPathContext context) throws XPathException {

        Controller controller = context.getController();
        // handle parameters if any

        ParameterSet params = assembleParams(context, actualParams);
        ParameterSet tunnels = assembleTunnelParams(context, tunnelParams);

        Rule currentTemplateRule = context.getCurrentTemplateRule();
        if (currentTemplateRule==null) {
            DynamicError e = new DynamicError("There is no current template rule");
            e.setXPathContext(context);
            e.setErrorCode("XTDE0560");
            e.setLocator(this);
            throw e;
        }
        Template currentTemplate = (Template)currentTemplateRule.getAction();
        int min = currentTemplate.getMinImportPrecedence();
        int max = currentTemplate.getPrecedence()-1;
        Mode mode = context.getCurrentMode();
        if (mode == null) {
            mode = controller.getRuleManager().getDefaultMode();
        }
        if (context.getCurrentIterator()==null) {
            DynamicError e = new DynamicError("Cannot call xsl:apply-imports when there is no context item");
            e.setXPathContext(context);
            e.setErrorCode("XTDE0565");
            e.setLocator(this);
            throw e;
        }
        Item currentItem = context.getCurrentIterator().current();
        if (!(currentItem instanceof NodeInfo)) {
            DynamicError e = new DynamicError("Cannot call xsl:apply-imports when context item is not a node");
            e.setXPathContext(context);
            e.setErrorCode("XTDE0565");
            e.setLocator(this);
            throw e;
        }
        NodeInfo node = (NodeInfo)currentItem;
        Rule rule = controller.getRuleManager().getTemplateRule(node, mode, min, max, context);

		if (rule==null) {             // use the default action for the node
            ApplyTemplates.defaultAction(node, params, tunnels, context, backwardsCompatible, getLocationId());
        } else {
            XPathContextMajor c2 = context.newContext();
            Template nh = (Template)rule.getAction();
            c2.setOrigin(this);
            c2.setLocalParameters(params);
            c2.setTunnelParameters(tunnels);
            c2.openStackFrame(nh.getStackFrameMap());
            nh.apply(c2, rule);
        }
        return null;
                // We never treat apply-imports as a tail call, though we could
    }



    /**
     * Diagnostic print of expression structure. The expression is written to the System.err
     * output stream
     *
     * @param level indentation level for this expression
     @param out
     @param config
     */

    public void display(int level, PrintStream out, Configuration config) {
        out.println(ExpressionTool.indent(level) + "apply-imports");
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
