package osh.core.interfaces;

import osh.registry.Registry.ComRegistry;

/**
 * @author Ingo Mauser
 */
public interface IOSHCom extends IOSH {

    ComRegistry getComRegistry();

}
