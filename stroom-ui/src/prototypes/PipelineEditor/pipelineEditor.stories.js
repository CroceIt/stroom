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
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { storiesOf, addDecorator } from '@storybook/react';
import { action } from '@storybook/addon-actions';
import { withNotes } from '@storybook/addon-notes';

import { ReduxDecoratorWithInitialisation } from 'lib/storybook/ReduxDecorator';
import { DragDropDecorator } from 'lib/storybook/DragDropDecorator';

import { PipelineEditor } from './index';

import PipelineElement from './PipelineElement';
import { ElementPalette } from './ElementPalette';
import { ElementDetails } from './ElementDetails';

import { actionCreators } from './redux';

import 'styles/main.css';

import { testPipeline, testPipelineElements } from './test/pipeline.testData';
import { testElementTypes, testElementProperties } from './test/elements.testData';
import { pipeline01, pipeline02 } from './test/setupSampleDataPipelines.testData';

const {
  pipelineReceived,
  elementsReceived,
  elementPropertiesReceived,
  pipelineElementSelected,
} = actionCreators;

// Set up Pipeline Editor stories. The stories here are split up into different lines
// because each story needs to dispatch it's own data, and a chained sequence of
// dispatches and adds reads awkwardly.
const pipelineEditorStories = storiesOf('Pipeline Editor', module).addDecorator(ReduxDecoratorWithInitialisation((store) => {
  store.dispatch(elementsReceived(testElementTypes));
  store.dispatch(elementPropertiesReceived(testElementProperties));
}));

// Add storyfor a simple pipeline
pipelineEditorStories
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(pipelineReceived('testPipeline', testPipeline));
  })) // must be recorder after/outside of the test initialisation decorators
  .addDecorator(DragDropDecorator)
  .add('Simple pipeline', () => <PipelineEditor pipelineId="testPipeline" />);

// Add story for a pipeline copied from setupSampleData
pipelineEditorStories
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(pipelineReceived('pipeline01', pipeline01));
  })) // must be recorder after/outside of the test initialisation decorators
  .add('setupSampleData -- pipeline01 - is a long pipeline', () => (
    <PipelineEditor pipelineId="pipeline01" />
  ));

// Add story for a pipeline copied from setupSampleData
pipelineEditorStories
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(pipelineReceived('pipeline02', pipeline02));
  })) // must be recorder after/outside of the test initialisation decorators
  .add('setupSampleData -- pipeline02 - contains a deleted element', () => (
    <PipelineEditor pipelineId="pipeline02" />
  ));

storiesOf('Element Palette', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(elementsReceived(testElementTypes));
    store.dispatch(elementPropertiesReceived(testElementProperties));
  })) // must be recorder after/outside of the test initialisation decorators
  .addDecorator(DragDropDecorator)
  .add('Element Palette', () => <ElementPalette />);

storiesOf('Element Details', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(elementsReceived(testElementTypes));
    store.dispatch(elementPropertiesReceived(testElementProperties));
    store.dispatch(pipelineReceived('pipeline01', pipeline01));
    store.dispatch(pipelineElementSelected('pipeline01', 'splitFilter', { splitDepth: 10, splitCount: 10 }));
  })) // must be recorder after/outside of the test initialisation decorators
  .addDecorator(DragDropDecorator)
  .add('Simple element details page', () => <ElementDetails pipelineId="pipeline01" />);