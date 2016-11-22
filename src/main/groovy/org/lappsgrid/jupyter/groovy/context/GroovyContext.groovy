/*
 * Copyright (c) 2016 The Language Application Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.lappsgrid.jupyter.groovy.context

import org.codehaus.groovy.control.CompilerConfiguration
import org.lappsgrid.jupyter.groovy.GroovyKernel

/**
 * Objects that implement the GroovyContext interface are used to obtain CompilerConfiguration
 * and MetaClass object used by the Groovy compiler when compiler user scripts.
 *
 * @author Keith Suderman
 */
interface GroovyContext {
    /**
     * Obtain a CompilerConfiguration object the Groovy compiler will use when compiling
     * user code.
     * @return an initialized CompilerConfiguration object.
     */
    CompilerConfiguration getCompilerConfiguration();

    /**
     * Obtain the MetaClass for compiled user code.
     * <p>
     * This MetaClass will be used by the Script object obtained by compiling user code.
     * User code will be compiled into a <code>Script</code> object by the Groovy compiler, the
     * script will have its <code>metaClass</code> field set, and then<code>run</code> is called.
     *
     * @param theClass the class the MetaClass applies to.
     * @param initialize if the MetaClass should be initialized.
     * @return
     */
    MetaClass getMetaClass(Class theClass, boolean initialize);
}