package osh.registry;

/**
 * Generic registry for communication between different modules of the OSH.
 * This registry works with the publish/subscribe architecture and enables subscription either for specific senders
 * (identified by their supplied UUID) of for any published exchanges of a sepcific type.
 *
 * @author Sebastian Kramer
 *
 * @param <I> the objects to be exchanged in this registry
 * @param <L> the listener interface all subscriber need to implement to use this registry
 */
public abstract class ARegistry<I, L> {
}
