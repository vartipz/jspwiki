/* 
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package org.apache.wiki.rpc.json;

import java.lang.reflect.Method;
import java.security.Permission;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiSession;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.rpc.RPCCallable;
import org.apache.wiki.rpc.RPCManager;
import org.apache.wiki.ui.TemplateManager;

import com.metaparadigm.jsonrpc.InvocationCallback;
import com.metaparadigm.jsonrpc.JSONRPCBridge;

/**
 *  Provides an easy-to-use interface for different modules to AJAX-enable
 *  themselves.  This class is a static class, so it cannot be instantiated,
 *  but it easily available from anywhere (including JSP pages).
 *  <p>
 *  Any object which wants to expose its methods through JSON calls, needs
 *  to implement the RPCCallable interface.  JSONRPCManager will expose
 *  <i>all</i> methods, so be careful which you want to expose.
 *  <p>
 *  Due to some limitations of the JSON-RPC library, we do not use the
 *  Global bridge object. 
 *  @see org.apache.wiki.rpc.RPCCallable
 *  @since 2.5.4
 */
// FIXME: Must be mootool-ified.
public final class JSONRPCManager extends RPCManager
{
    private static HashMap<String, CallbackContainer> c_globalObjects = new HashMap<String, CallbackContainer>();
    
    /** Prevent instantiation */
    private JSONRPCManager()
    {
        super();
    }
    
    /**
     *  Emits JavaScript to do a JSON RPC Call.  You would use this method e.g.
     *  in your plugin generation code to embed an AJAX call to your object.
     *  
     *  @param context The Wiki Context
     *  @param c An RPCCallable object
     *  @param function Name of the method to call
     *  @param params Parameters to pass to the method
     *  @return generated JavasSript code snippet that calls the method
     */
    public static String emitJSONCall( WikiContext context, RPCCallable c, String function, String params )
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<script>");
        sb.append("var result = jsonrpc."+getId(c)+"."+function+"("+params+");\r\n");
        sb.append("document.write(result);\r\n");
        sb.append("</script>");
        
        return sb.toString();        
    }
    
    /**
     *  Registers a callable to JSON global bridge and requests JSON libraries to be added
     *  to the page.  
     *  
     *  @param context The WikiContext.
     *  @param c The RPCCallable to register
     *  @return the ID of the registered callable object
     */
    public static String registerJSONObject( WikiContext context, RPCCallable c )
    {
        String id = getId(c);
        JSONRPCBridge.getGlobalBridge().registerObject( id, c );

        requestJSON( context );
        return id;
    }
    
    /**
     *  Requests the JSON Javascript and object to be generated in the HTML.
     *  @param context The WikiContext.
     */
    public static void requestJSON( WikiContext context )
    {
        TemplateManager.addResourceRequest(context, 
                                           TemplateManager.RESOURCE_SCRIPT, 
                                           context.getURL(WikiContext.NONE,"scripts/json-rpc/jsonrpc.js"));        
        
        String jsonurl = context.getURL( WikiContext.NONE, "JSON-RPC" );
        TemplateManager.addResourceRequest(context, 
                                           TemplateManager.RESOURCE_JSFUNCTION, 
                                           "jsonrpc = new JSONRpcClient(\""+jsonurl+"\");");
        
        JSONRPCBridge.getGlobalBridge().registerCallback(new WikiJSONAccessor(), HttpServletRequest.class);
    }
    
    /**
     *  Provides access control to the JSON calls.  Rather private.
     *  Unfortunately we have to check the permission every single time, because
     *  the user can log in and we would need to reset the permissions at that time.
     *  Note that this is an obvious optimization piece if this becomes
     *  a bottleneck.
     *  
     */
    static class WikiJSONAccessor implements InvocationCallback
    {
        private static final long serialVersionUID = 1L;
        private static final Logger log = Logger.getLogger( WikiJSONAccessor.class );
        
        /**
         *  Create an accessor.
         */
        public WikiJSONAccessor()
        {}
        
        /**
         *  Does not do anything.
         * 
         *  {@inheritDoc}
         */
        public void postInvoke(Object context, Object instance, Method method, Object result) throws Exception
        {
        }

        /**
         *  Checks access against the permission given.
         *  
         *  {@inheritDoc}
         */
        public void preInvoke(Object context, Object instance, Method method, Object[] arguments) throws Exception
        {
            if( context instanceof HttpServletRequest )
            {
                boolean canDo = false;
                HttpServletRequest req = (HttpServletRequest) context;
                
                WikiEngine e = WikiEngine.getInstance( req.getSession().getServletContext(), null );
               
                for( Iterator i = c_globalObjects.values().iterator(); i.hasNext(); )
                {
                    CallbackContainer cc = (CallbackContainer) i.next();
                    
                    if( cc.m_object == instance )
                    {
                        canDo = e.getAuthorizationManager().checkPermission( WikiSession.getWikiSession(e, req), 
                                                                             cc.m_permission );

                        break;
                    }
                }
                                    
                if( canDo )
                {
                    return;
                }
            }

            log.debug("Failed JSON permission check: "+instance);
            throw new WikiSecurityException("No permission to access this AJAX method!");
        }
        
    }

    /**
     *  Registers a global object (i.e. something which can be called by any
     *  JSP page).  Typical examples is e.g. "search".  By default, the RPCCallable
     *  shall need a "view" permission to access.
     *  
     *  @param id     The name under which this shall be registered (e.g. "search")
     *  @param object The RPCCallable which shall be associated to this id.
     */
    public static void registerGlobalObject(String id, RPCCallable object)
    {
        registerGlobalObject(id, object, PagePermission.VIEW);    
    }
    
    /**
     *  Registers a global object (i.e. something which can be called by any
     *  JSP page) with a specific permission.  
     *  
     *  @param id     The name under which this shall be registered (e.g. "search")
     *  @param object The RPCCallable which shall be associated to this id.
     *  @param perm   The permission which is required to access this object.
     */
    public static void registerGlobalObject(String id, RPCCallable object, Permission perm )
    {
        CallbackContainer cc = new CallbackContainer();
        cc.m_permission = perm;
        cc.m_id = id;
        cc.m_object = object;
        
        c_globalObjects.put( id, cc );
    }

    /**
     *  Just stores the registered global method.
     *  
     *
     */
    private static class CallbackContainer
    {
        String m_id;
        RPCCallable m_object;
        Permission m_permission;
    }
}
