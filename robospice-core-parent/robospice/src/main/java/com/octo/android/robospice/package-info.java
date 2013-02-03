/**
 * This library provides aims to provide a robust framework for executing asynchronous network requests in android.
 * It offers :
 * <ul>
 *   <li> executions of requests in a background service
 *   <li> caching of requests results
 *   <li> observable-observer design pattern to get aware of request result
 *   <li> very easy to write requests (more or less like an asynctask)
 * </ul>
 * 
 * @startuml
 * abstract class SpiceActivity {
 *   - SpiceManager
 *   - List<request>
 * }
 * 
 * class SpiceManager {
 *   - List<request>
 * }
 *
 * class SpiceService {
 * }
 *
 *
 * abstract class SpiceRequest<T> {
 *   +loadData(): T;
 * }
 *
 * SpiceService -left- SpiceRequest : execute
 * SpiceActivity -left- SpiceManage
 * SpiceManager -left- SpiceService: bind \n unbind \n submit requests
 *
 * abstract class CachedSpiceRequest<T> extends SpiceRequest {
 *    # DataPersistenceManager persistenceManager;
 *    -loadData(): T
 *    +loadDataFromCache(): T
 *    +saveDataToCacheAndReturnData(T): T
 * }
 *
 * CachedSpiceRequest *-right- DataPersistenceManager
 *
 * class DataPersistenceManager {
 * -List<DataClassPersistenceManager>
 * -List<DataClassPersistenceManagerFactory>
 * }
 *
 * abstract class DataClassPersistenceManager<T> {
 *   +loadData() : T
 *   +saveDataAndReturnData: T
 *   +canHandle(Class clazz);
 * }
 *
 * abstract class DataClassPersistenceManagerFactory {
 * }
 *
 * DataPersistenceManager *-- DataClassPersistenceManager
 * DataPersistenceManager *-- DataClassPersistenceManagerFactory
 * DataClassPersistenceManagerFactory -- DataClassPersistenceManager: create
 * @enduml
 */
package com.octo.android.robospice;

