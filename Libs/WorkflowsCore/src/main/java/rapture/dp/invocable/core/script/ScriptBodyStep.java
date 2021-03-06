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
package rapture.dp.invocable.core.script;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.net.MediaType;

import rapture.common.CallingContext;
import rapture.common.RaptureScript;
import rapture.common.RaptureURI;
import rapture.common.Scheme;
import rapture.common.ScriptResult;
import rapture.common.dp.AbstractInvocable;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.dp.InvocableUtils;
import rapture.kernel.DocApiImpl;
import rapture.kernel.Kernel;
import rapture.script.reflex.ReflexRaptureScript;
import rapture.workflow.script.WorkflowScriptConstants;

/**
 * @author bardhi
 * @since 8/19/14.
 */
public class ScriptBodyStep<T> extends AbstractInvocable<T> {
    private static final Logger log = Logger.getLogger(ScriptBodyStep.class);

    public ScriptBodyStep(String workerURI) {
        super(workerURI);
    }

    @Override public String invoke(CallingContext context) {
        log.info("Attempting to run embedded Reflex Script");
        RaptureScript script = new RaptureScript();
        String body = Kernel.getDecision().getContextValue(context, getWorkerURI(), WorkflowScriptConstants.BODY);
        script.setScript(body);
        script.setName("anonymous_script.rfx");
        ReflexRaptureScript rScript = new ReflexRaptureScript();
        // TODO: Workflows do not support suspend logic, so we cannot allow for suspend/resume here. Once we add that support,
        // this can be changed IF we want to. We might also not want to: maybe the right thing to do is have each step wait, and not wait within the step
        String paramsJson = Kernel.getDecision().getContextValue(context, getWorkerURI(), WorkflowScriptConstants.PARAMS);
        Map<String, Object> params = JacksonUtil.getMapFromJson(paramsJson);

        String workerAuditUri = InvocableUtils.getWorkflowAuditUri(getWorkerURI());
        if (workerAuditUri != null) {
            rScript.setAuditLogUri(workerAuditUri);
        }
        
        RaptureURI outputUri = RaptureURI.newScheme(getWorkerURI(), Scheme.DOCUMENT);
        DocApiImpl docApi = Kernel.getDoc().getTrusted();
        StringBuilder sb = new StringBuilder();

		try {
			ScriptResult resp = rScript.runProgramExtended(context, null, script, params);
			if (resp != null) {
			    log.info("Reflex script returned " + resp);
			}
	        for (String s : resp.getOutput()) sb.append(s);
	        String str = docApi.getDocEphemeral(context, outputUri.toShortString());
	        Map<String, Object> m = (str == null) ? new HashMap<String, Object>() : JacksonUtil.getMapFromJson(str);
	        m.put(outputUri.toString(),  sb.toString());
	        String json = JacksonUtil.jsonFromObject(m);
	        docApi.putDocEphemeral(context, outputUri.toShortString(), JacksonUtil.jsonFromObject(m));
	        return "next";
		} catch (Exception e) {
			sb.append(e.getMessage());
	        Map<String, Object> m = new HashMap<>();
	        m.put(outputUri.toString(),  sb.toString());
	        docApi.putDocEphemeral(context, outputUri.toShortString(), JacksonUtil.jsonFromObject(m));
	        throw e;
		}
    }
}
