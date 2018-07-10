import { actionCreators } from './redux/explorerTreeReducer';
import { wrappedGet, wrappedPut, wrappedPost } from 'lib/fetchTracker.redux';
import PermissionInheritanceValues from './PermissionInheritanceValues';

const {
  docTreeReceived,
  docRefTypesReceived,
  docRefInfoReceived,
  completeDocRefCopy,
  completeDocRefDelete,
  completeDocRefRename,
  completeDocRefMove,
} = actionCreators;

export const fetchDocTree = () => (dispatch, getState) => {
  const state = getState();
  const url = `${state.config.explorerServiceUrl}/all`;
  wrappedGet(dispatch, state, url, response =>
    response.json().then(documentTree => dispatch(docTreeReceived(documentTree))));
};

export const fetchDocRefTypes = () => (dispatch, getState) => {
  const state = getState();
  const url = `${state.config.explorerServiceUrl}/docRefTypes`;
  wrappedGet(dispatch, state, url, response =>
    response.json().then(docRefTypes => dispatch(docRefTypesReceived(docRefTypes))));
};

export const fetchDocInfo = docRef => (dispatch, getState) => {
  const state = getState();
  const url = `${state.config.explorerServiceUrl}/info/${docRef.type}/${docRef.uuid}`;
  wrappedGet(dispatch, state, url, response =>
    response.json().then(docRefInfo => dispatch(docRefInfoReceived(docRefInfo))));
};

// export const copyDocument = (docRefs, destinationFolderRef, permissionInheritance) => {
//   const state = getState();
//   const url = `${state.config.explorerServiceUrl}/copy/${docRef.type}/${docRef.uuid}`;
//   wrappedPost(
//     dispatch,
//     state,
//     url,
//     response => response.json().then(docRefInfo => dispatch(completeDocRefCopy(docRefInfo))),
//     {
//       body: JSON.stringify({
//         docRefs: [{ type: 'joe', name: 'bob', uuid: '123' }, {}],
//         destinationFolderRef: { type: 'joe', name: 'bob', uuid: '123' },
//         permissionInheritance: 'Combined',
//       }),
//     },
//   );
// };

// export const moveDocument = (docRefs, destinationFolderRef, permissionInheritance) => {
//   const state = getState();
//   const url = `${state.config.explorerServiceUrl}/move/${docRef.type}/${docRef.uuid}`;
//   wrappedPost(
//     dispatch,
//     state,
//     url,
//     response => response.json().then(docRefInfo => dispatch(completeDocRefMove(docRefInfo))),
//     {
//       body: JSON.stringify({
//         docRefs: [],
//         destinationFolderRef: {},
//         permissionInheritance: 'None',
//       }),
//     },
//   );
// };