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
import React from 'react';
import PropTypes from 'prop-types';

import { compose, withState, branch, renderComponent } from 'recompose';
import { connect } from 'react-redux';
import { Loader } from 'semantic-ui-react';

import ExpressionOperator from './ExpressionOperator';
import ROExpressionOperator from './ROExpressionOperator';
import { LineContainer } from 'components/LineTo';

import { Checkbox } from 'semantic-ui-react';

import lineElementCreators from './expressionLineCreators';

const withSetEditableByUser = withState('editableByUser', 'setEditableByUser', false);

const ExpressionBuilder = ({
  expressionId,
  dataSource,
  expression,
  isEditableSystemSet,

  // withSetEditableByUser
  editableByUser,
  setEditableByUser,
}) => {
  if (!dataSource) {
    return <div>Awaiting Data Source</div>;
  }
  if (!expression) {
    return <div>Awaiting Expression</div>;
  }

  const roOperator = (
    <ROExpressionOperator expressionId={expressionId} isEnabled operator={expression} />
  );

  const editOperator = (
    <ExpressionOperator
      dataSource={dataSource}
      expressionId={expressionId}
      isRoot
      isEnabled
      operator={expression}
    />
  );

  let theComponent;
  if (isEditableSystemSet) {
    theComponent = (
      <div>
        <Checkbox
          label="Edit Mode"
          toggle
          checked={editableByUser}
          onChange={() => setEditableByUser(!editableByUser)}
        />
        {editableByUser ? editOperator : roOperator}
      </div>
    );
  } else {
    theComponent = roOperator;
  }

  return (
    <LineContainer
      lineContextId={`expression-lines-${expressionId}`}
      lineElementCreators={lineElementCreators}
    >
      {theComponent}
    </LineContainer>
  );
};

ExpressionBuilder.propTypes = {
  // Set by container
  dataSourceUuid: PropTypes.string.isRequired,
  expressionId: PropTypes.string.isRequired,
  isEditableSystemSet: PropTypes.bool.isRequired,

  // Redux state
  dataSource: PropTypes.object.isRequired,
  expression: PropTypes.object.isRequired,

  // withSetEditableByUser
  setEditableByUser: PropTypes.func.isRequired,
  editableByUser: PropTypes.bool.isRequired,
};

ExpressionBuilder.defaultProps = {
  isEditableSystemSet: false,
};

export default compose(
  connect(
    (state, props) => ({
      dataSource: state.dataSources[props.dataSourceUuid],
      expression: state.expressions[props.expressionId],
    }),
    {
      // actions
    },
  ),
  withSetEditableByUser,
  branch(
    props => !props.expression,
    renderComponent(() => <Loader active>Loading Expression</Loader>),
  ),
  branch(
    props => !props.dataSource,
    renderComponent(() => <Loader active>Loading Data Source</Loader>),
  ),
)(ExpressionBuilder);