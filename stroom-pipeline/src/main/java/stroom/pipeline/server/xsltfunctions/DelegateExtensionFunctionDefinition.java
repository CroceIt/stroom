/*
 * Copyright 2016 Crown Copyright
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
 */

package stroom.pipeline.server.xsltfunctions;

import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;
import stroom.xml.NamespaceConstants;

public class DelegateExtensionFunctionDefinition extends ExtensionFunctionDefinition {
    private transient final StroomXSLTFunctionLibrary library;
    private final String functionName;
    private final int minArgs;
    private final int maxArgs;
    private final SequenceType[] argTypes;
    private final SequenceType resultType;
    private transient final StructuredQName qName;
    private final Class<?> delegateClass;

    private DelegateExtensionFunctionCall functionCall;

    private DelegateExtensionFunctionDefinition(final StroomXSLTFunctionLibrary library,
                                               final String functionName,
                                               final int minArgs,
                                               final int maxArgs,
                                               final SequenceType[] argTypes,
                                               final SequenceType resultType,
                                               final Class<?> delegateClass) {
        this.functionName = functionName;
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.argTypes = argTypes;
        this.resultType = resultType;
        this.delegateClass = delegateClass;
        this.library = library;

        qName = new StructuredQName("", NamespaceConstants.STROOM, functionName);
    }

    @Override
    public StructuredQName getFunctionQName() {
        return qName;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return minArgs;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return maxArgs;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return argTypes;
    }

    @Override
    public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes) {
        return resultType;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        if (functionCall == null) {
            functionCall = new DelegateExtensionFunctionCall(functionName, delegateClass);
            library.registerInUse(functionCall);
        }

        return functionCall;
    }

    public DelegateExtensionFunctionCall getFunctionCall() {
        return functionCall;
    }

    public static Builder startBuild() {
        return new Builder();
    }

    public static class Builder {
        private StroomXSLTFunctionLibrary library;
        private String functionName;
        private int minArgs = 0;
        private int maxArgs = 0;
        private SequenceType[] argTypes = new SequenceType[]{};
        private SequenceType resultType;
        private Class<?> delegateClass;

        private Builder() {

        }

        public Builder library(final StroomXSLTFunctionLibrary value) {
            this.library = value;
            return this;
        }

        public Builder functionName(final String value) {
            this.functionName = value;
            return this;
        }

        public Builder minArgs(final int value) {
            this.minArgs = value;
            return this;
        }

        public Builder maxArgs(final int value) {
            this.maxArgs = value;
            return this;
        }

        public Builder argTypes(final SequenceType[] value) {
            this.argTypes = value;
            return this;
        }

        public Builder resultType(final SequenceType value) {
            this.resultType = value;
            return this;
        }

        public Builder delegateClass(final Class<?> value) {
            this.delegateClass = value;
            return this;
        }

        public DelegateExtensionFunctionDefinition build() {
            return new DelegateExtensionFunctionDefinition(
                    library,
                    functionName,
                    minArgs,
                    maxArgs,
                    argTypes,
                    resultType,
                    delegateClass);
        }
    }
}
