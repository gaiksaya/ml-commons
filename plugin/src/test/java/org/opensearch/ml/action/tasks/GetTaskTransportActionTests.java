/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.ml.action.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.ActionListener;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.ml.common.transport.task.MLTaskGetRequest;
import org.opensearch.ml.common.transport.task.MLTaskGetResponse;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.TransportService;

public class GetTaskTransportActionTests extends OpenSearchTestCase {
    @Mock
    ThreadPool threadPool;

    @Mock
    Client client;

    @Mock
    NamedXContentRegistry xContentRegistry;

    @Mock
    TransportService transportService;

    @Mock
    ActionFilters actionFilters;

    @Mock
    ActionListener<MLTaskGetResponse> actionListener;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    GetTaskTransportAction getTaskTransportAction;
    MLTaskGetRequest mlTaskGetRequest;
    ThreadContext threadContext;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mlTaskGetRequest = MLTaskGetRequest.builder().taskId("test_id").build();

        getTaskTransportAction = spy(new GetTaskTransportAction(transportService, actionFilters, client, xContentRegistry));

        Settings settings = Settings.builder().build();
        threadContext = new ThreadContext(settings);
        when(client.threadPool()).thenReturn(threadPool);
        when(threadPool.getThreadContext()).thenReturn(threadContext);
    }

    public void testGetTask_NullResponse() {
        doAnswer(invocation -> {
            ActionListener<GetResponse> listener = invocation.getArgument(1);
            listener.onResponse(null);
            return null;
        }).when(client).get(any(), any());
        getTaskTransportAction.doExecute(null, mlTaskGetRequest, actionListener);
        ArgumentCaptor<Exception> argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(actionListener).onFailure(argumentCaptor.capture());
        assertEquals("Fail to find task", argumentCaptor.getValue().getMessage());
    }

    public void testGetTask_RuntimeException() {
        doAnswer(invocation -> {
            ActionListener<GetResponse> listener = invocation.getArgument(1);
            listener.onFailure(new RuntimeException("errorMessage"));
            return null;
        }).when(client).get(any(), any());
        getTaskTransportAction.doExecute(null, mlTaskGetRequest, actionListener);
        ArgumentCaptor<Exception> argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(actionListener).onFailure(argumentCaptor.capture());
        assertEquals("errorMessage", argumentCaptor.getValue().getMessage());
    }

    public void testGetTask_IndexNotFoundException() {
        doAnswer(invocation -> {
            ActionListener<GetResponse> listener = invocation.getArgument(1);
            listener.onFailure(new IndexNotFoundException("Index Not Found"));
            return null;
        }).when(client).get(any(), any());
        getTaskTransportAction.doExecute(null, mlTaskGetRequest, actionListener);
        ArgumentCaptor<Exception> argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(actionListener).onFailure(argumentCaptor.capture());
        assertEquals("Fail to find task", argumentCaptor.getValue().getMessage());
    }
}
