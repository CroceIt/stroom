import React from 'react';
import PropTypes from 'prop-types';
import { compose, withProps } from 'recompose';
import { connect } from 'react-redux';
import { Confirm } from 'semantic-ui-react';

import { actionCreators } from './redux';

const { pipelineElementDeleteCancelled, pipelineElementDeleted } = actionCreators;

const enhance = compose(
  connect(
    (state, props) => ({
      pipeline: state.pipelineEditor.pipelines[props.pipelineId],
      elements: state.pipelineEditor.elements,
    }),
    {
      pipelineElementDeleteCancelled,
      pipelineElementDeleted,
    },
  ),
  withProps(({
    pipelineId,
    pipeline: { pendingElementToDelete },
    pipelineElementDeleteCancelled,
    pipelineElementDeleted,
  }) => ({
    onCancelDelete: () => pipelineElementDeleteCancelled(pipelineId),
    onConfirmDelete: () => {
      pipelineElementDeleted(pipelineId, pendingElementToDelete);
    },
  })),
);

const DeletePipelineElement = ({ pendingElementToDelete, onConfirmDelete, onCancelDelete }) => (
  <Confirm
    open={!!pendingElementToDelete}
    content={`Delete ${pendingElementToDelete} from pipeline?`}
    onCancel={onCancelDelete}
    onConfirm={onConfirmDelete}
  />
);

DeletePipelineElement.propTypes = {
  pipelineId: PropTypes.string.isRequired,
};

export default enhance(DeletePipelineElement);
