package org.orbeon.saxon.functions;
import org.orbeon.saxon.expr.XPathContext;
import org.orbeon.saxon.om.*;
import org.orbeon.saxon.trans.XPathException;
import org.orbeon.saxon.value.StringValue;
import org.orbeon.saxon.value.RestrictedStringValue;
import org.orbeon.saxon.style.StandardNames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* This class supports fuctions get-in-scope-prefixes()
*/

public class InScopePrefixes extends SystemFunction {

    /**
    * Iterator over the results of the expression
    */

    public SequenceIterator iterate(XPathContext context) throws XPathException {
        NodeInfo element = (NodeInfo)argument[0].evaluateItem(context);
        NamespaceResolver resolver = new InscopeNamespaceResolver(element);
        Iterator iter = resolver.iteratePrefixes();
        List list = new ArrayList(10);
        while (iter.hasNext()) {
            String prefix = (String)iter.next();
            if (prefix.equals("")) {
                list.add(StringValue.EMPTY_STRING);
            } else {
                list.add(RestrictedStringValue.makeRestrictedString(
                        prefix, StandardNames.XS_NCNAME, null));
            }
        }
        return new ListIterator(list);

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
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
