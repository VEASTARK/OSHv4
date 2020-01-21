package osh.core.interfaces;

import osh.registry.DataRegistry.ComRegistry;

/**
 * @author Ingo Mauser
 */
public interface IOSHCom extends IOSH {

    ComRegistry getComRegistry();

}
