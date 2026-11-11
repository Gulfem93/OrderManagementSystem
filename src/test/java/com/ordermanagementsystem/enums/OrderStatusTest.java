package com.ordermanagementsystem.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void ownerFlowMovesForwardStepByStep() {
        assertThat(OrderStatus.PENDING.canChangeTo(OrderStatus.PREPARING)).isTrue();
        assertThat(OrderStatus.PREPARING.canChangeTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.DELIVERED.canChangeTo(OrderStatus.RETURNED)).isTrue();
    }

    @Test
    void stepsCannotBeSkippedOrReversed() {
        assertThat(OrderStatus.PENDING.canChangeTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.PENDING)).isFalse();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void finalStatusesHaveNoNextStatus() {
        assertThat(OrderStatus.CANCELLED.getNextStatuses()).isEmpty();
        assertThat(OrderStatus.RETURNED.getNextStatuses()).isEmpty();
    }

    @Test
    void onlyCancelAndReturnRestoreStock() {
        for (OrderStatus status : OrderStatus.values()) {
            boolean expected = status == OrderStatus.CANCELLED || status == OrderStatus.RETURNED;
            assertThat(status.restoresStock()).as(status.name()).isEqualTo(expected);
        }
    }

    @Test
    void customerCanOnlyCancelPendingAndReceiveShipped() {
        assertThat(OrderStatus.PENDING.isCancellable()).isTrue();
        assertThat(OrderStatus.PREPARING.isCancellable()).isFalse();
        assertThat(OrderStatus.SHIPPED.isReceivable()).isTrue();
        assertThat(OrderStatus.PREPARING.isReceivable()).isFalse();
    }
}
