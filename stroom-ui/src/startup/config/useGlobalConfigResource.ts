/*
 * Copyright 2018 Crown Copyright
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
import { useHttpClient2 } from "lib/useHttpClient";
import * as React from "react";
import { UiConfig } from "./types";
import { useUrlFactory } from "../../lib/useUrlFactory";
import { useEffect, useState } from "react";

export interface Api {
  config: UiConfig;
}

const useGlobalConfigResource = (): Api => {
  const { httpGet } = useHttpClient2();
  const { apiUrl } = useUrlFactory();
  const resource = apiUrl("/config/v1");
  const [config, setConfig] = useState<UiConfig>();

  const fetchUiConfig = React.useCallback(() => {
    return httpGet(`${resource}/noauth/fetchUiConfig`);
  }, [httpGet, resource]);

  useEffect(() => {
    console.log("Fetching config");
    fetchUiConfig().then((c) => {
      console.log("Setting config to " + JSON.stringify(c));
      setConfig(c);
    });
  }, [fetchUiConfig]);

  return { config };
};

export { useGlobalConfigResource };

export default useGlobalConfigResource;
