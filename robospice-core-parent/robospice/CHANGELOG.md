Release 1.4.2 (planned)

* SpiceManager can now add data to cache. Thanks to Christopher Jenkins fur suggesting this feature in https://github.com/octo-online/robospice/issues/75.
* Spice Manager : getDataFromCache, addToCache and cancel will work even if network is down. This bug was mentionned by dkraus in https://github.com/octo-online/robospice/issues/67

Release 1.4.1 (May 11 2013)

* More testing.
* a new method to execute request is now possible : getFromCacheButLoadFromNetworkAnyway. It allows to get data from cache if it is not expired but will always perform the request anyway.
* issue 36 solved : ProgressUpdates where not receive in the right order. Thx to Alessio Bianchi
