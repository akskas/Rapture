/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package reflex.node;

import java.util.ArrayList;
import java.util.List;

import reflex.IReflexHandler;
import reflex.Scope;
import reflex.debug.IReflexDebugger;
import reflex.value.ReflexValue;

/**
 * Format
 * 
 * @author amkimian
 * 
 */
public class FormatNode extends BaseNode {

    private List<ReflexNode> params;

    public FormatNode(int lineNumber, IReflexHandler handler, Scope scope, List<ReflexNode> ps) {
        super(lineNumber, handler, scope);
        this.params = ps == null ? new ArrayList<ReflexNode>() : ps;
    }

    @Override
    public ReflexValue evaluate(IReflexDebugger debugger, Scope scope) {
        debugger.stepStart(this, scope);
        ReflexValue formatS = params.get(0).evaluate(debugger, scope);
        Object[] args = new Object[params.size()-1];
        for(int i=1; i< params.size(); i++) {
        	ReflexValue v = params.get(i).evaluate(debugger, scope);
        	args[i-1] = v.asObject();
        }
        String val = String.format(formatS.asString(), args);
        ReflexValue retVal = new ReflexValue(val);
        debugger.stepEnd(this, retVal, scope);
        return retVal;
    }

}
