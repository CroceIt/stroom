/*
 * Copyright 2017 Crown Copyright
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

package stroom.servlet;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HttpServletRequestFilter implements Filter {
    private final HttpServletRequestHolder httpServletRequestHolder;

    @Inject
    HttpServletRequestFilter(final HttpServletRequestHolder httpServletRequestHolder) {
        this.httpServletRequestHolder = httpServletRequestHolder;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            try {
                httpServletRequestHolder.set((HttpServletRequest) request);
                // Continue the chain
                chain.doFilter(request, response);
            } finally {
                // Clear the held request in case the thread holding the thread scoped holder is re-used for something else
                httpServletRequestHolder.set(null);
            }
        } else {
            // Continue the chain
            chain.doFilter(request, response);
        }
    }
}