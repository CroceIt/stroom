/* eslint camelcase: ["error", {properties: "never"}]*/
/*
 * Copyright 2019 Crown Copyright
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
import { useCallback } from "react";
import { useHttpClient2 } from "lib/useHttpClient";
import useUrlFactory from "lib/useUrlFactory";
import { CreateTokenRequest, ResultPage, SearchTokenRequest } from "./types";
import { Token } from "../../tokens/api/types";

interface TokenResource {
  list: () => Promise<ResultPage<Token>>;
  search: (request: SearchTokenRequest) => Promise<ResultPage<Token>>;
  create: (request: CreateTokenRequest) => Promise<number>;
  read: (tokenId: number) => Promise<Token>;
  update: (token: Token, tokenId: number) => Promise<boolean>;
  remove: (tokenId: number) => Promise<boolean>;
}

export const useTokenResource = (): TokenResource => {
  const { httpGet, httpPost, httpPut, httpDelete } = useHttpClient2();

  const { apiUrl } = useUrlFactory();
  const resource = apiUrl("/token/v1");

  const list = useCallback(() => httpGet(`${resource}`), [resource, httpGet]);

  const search = useCallback(
    (request: SearchTokenRequest) => httpPost(`${resource}/search`, request),
    [resource, httpPost],
  );

  const create = useCallback(
    (request: CreateTokenRequest) => httpPost(`${resource}`, request),
    [resource, httpPost],
  );

  const read = useCallback(
    (tokenId: number) => httpGet(`${resource}/${tokenId}`),
    [resource, httpGet],
  );

  const update = useCallback(
    (token: Token, tokenId: number) => httpPut(`${resource}/${tokenId}`, token),
    [resource, httpPut],
  );

  const remove = useCallback(
    (tokenId: number) => httpDelete(`${resource}/${tokenId}`),
    [resource, httpDelete],
  );

  return {
    list,
    search,
    create,
    read,
    update,
    remove,
  };
};
