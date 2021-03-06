import * as React from "react";
import { storiesOf } from "@storybook/react";
import fullTestData from "testing/data";
import DataRetention from "./DataRetention";
import { MetaRow } from "components/MetaBrowser/types";

const dataRow: MetaRow = fullTestData.dataList.streamAttributeMaps[0];

storiesOf("Sections/Meta Browser/Detail Tabs", module).add(
  "Data Retention",
  () => <DataRetention dataRow={dataRow} />,
);
