package com.redhat.consulting;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.infinispan.Cache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

/**
 * A simple JSF controller to show how the RHDG cache configured with EAP infinispan subsystem is used.
 *
 * @author <a href="mailto:wfink@redhat.com">Wolf-Dieter Fink</a>
 */
@Model
public class JsfController {
  private static final Logger LOGGER = Logger.getLogger(JsfController.class);
  private CacheView cacheView;

  //   @Resource(lookup = "java:jboss/infinispan/container/jdg-container")
  //   CacheContainer container;   // container could be injected if needed

  /**
   * The direct lookup like
   *
   * @Resource(lookup = "java:jboss/infinispan/cache/jdg-container/EAPcache")
   *
   * is not working because of https://issues.redhat.com/browse/JBEAP-22290.
   * so the indirection with <resource-ref> in web.xml is needed until the bug is fixed.
   * Also the boss-deployment-structure.xml is necessary to have the dependency for org.infinispan classes otherwise there might be confusing failures
   */
  @Resource(name = "ApplicationCache2")
  Cache<String, String> cache;

  @Resource(name = "ApplicationCache")
  Cache<String, String> cache2;

  @Resource(name = "infinispan/CacheManager")
  EmbeddedCacheManager cacheManager;

  /**
   * Initialize the controller.
   */
  @PostConstruct
  public void init() {
    LOGGER.debug("init");
    initForm();
  }

  public void initForm() {
    this.cacheView = new CacheView();
  }

  @Produces
  @Named
  public CacheView getCache() {
    return this.cacheView;
  }

  public void add() {
    LOGGER.info("Try to add entry key=" + cacheView.getKey());
    cache.put(cacheView.getKey(), cacheView.getValue());
    getSize();
  }

  public void get() {
    String key = cacheView.getKey();
    LOGGER.info("Try to read key=" + key);
    CacheEntry<String, String> cacheEntry = cache.getAdvancedCache().getCacheEntry(key);
    if (cacheEntry == null) {
      cacheView.setValue("NOT AVAILABLE");
    } else {
      cacheView.setValue(cacheEntry.getValue());
      cacheView.setLifespan(cacheEntry.getLifespan());
      cacheView.setMaxIdle(cacheEntry.getMaxIdle());
      cacheView.setCreation(cacheEntry.getCreated());
      cacheView.setLastUsed(cacheEntry.getLastUsed());
    }
    getSize();
  }

  public void delete() {
    String key = cacheView.getKey();
    LOGGER.info("Try to delete entry key=" + key);
    cache.remove(key);
  }

  public void list() {
    LOGGER.info("Try to list items");
    StringBuilder result = new StringBuilder();
    cache.clear();
    // read cache
    for (String key : cache.keySet()) {
      result.append(key + "  :  ");
      result.append(cache.get(key));
      result.append(" || ");
    }
    cacheView.setEntries(result.toString());
  }

  public void getSize() {
    LOGGER.info("size is " + cache.size());
    cacheView.setSize(cache.size());
  }

  /**
   * This method compiles several features that can be achieved
   * using DG on EAP
   */
  public void test() {
    LOGGER.info("Caches list: " + cacheManager.getCacheNames());
    LOGGER.info("\n\nCache 1 configuration: \n" + cache.getCacheConfiguration().toXMLString("AppCache1") + "\n");
    LOGGER.info("\n\nCache 2 configuration: \n" + cache2.getCacheConfiguration().toXMLString("AppCache2") + "\n");
    LOGGER.info("Cache 1 size is " + cache.size());
  }

}
