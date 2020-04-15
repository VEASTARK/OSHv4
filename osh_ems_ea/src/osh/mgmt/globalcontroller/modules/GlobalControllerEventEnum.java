package osh.mgmt.globalcontroller.modules;

/**
 * Represents events caused by the operation of the {@link osh.mgmt.globalcontroller.ModularGlobalController}.
 *
 * @author Sebastian Kramer
 */
public enum GlobalControllerEventEnum {

    SCHEDULING_FINISHED,
    FORCE_SCHEDULING,
    RECEIVED_REGULAR_EPS,
    RECEIVED_REGULAR_PLS
}
