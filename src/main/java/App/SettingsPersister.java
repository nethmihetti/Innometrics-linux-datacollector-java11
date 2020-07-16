package App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class SettingsPersister {
	public static class JsonFilePersister extends SettingsPersister {
		private final boolean prettyFile;
		private final JSONObject cache;
		private final Path p;
		private volatile String currentStore;

		private JsonFilePersister(JSONObject cache, Path p, boolean prettyFile) {
			this.prettyFile = prettyFile;
			this.cache = cache;
			this.p = p;
		}

		private void commit() throws JSONException {
			if (p == null)
				return;

			final String contents = prettyFile ? cache.toString(4) : cache.toString();
//			Dispatchers.instance.getFileIoDispatcher().submit(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						IoAndEncodingUtil.overwriteTextFile(p, contents);
//					} catch (Throwable e) {
//						Model.onError(null, e, "Failed to save updated setting");
//					}
//				}
//			});
		}

		private JSONObject getContainer(String useStore) {
			if (useStore == null)
				return cache;

			JSONObject allStores = cache.optJSONObject("stores");
			if (allStores == null)
				return null;
			return allStores.optJSONObject(useStore);
		}

		private JSONObject makeContainer(String useStore) throws JSONException {
			assert useStore != null;
			JSONObject allStores = cache.optJSONObject("stores");
			if (allStores == null) {
				allStores = new JSONObject();
				cache.put("stores", allStores);
			}
			JSONObject container = new JSONObject();
			allStores.put(useStore, container);
			return container;
		}

		private void breakContainer(String useStore) {
			cache.remove(useStore);
		}

		@Override
		public void setStore(String store, Path workingFolder) {
			if ((store == null || store.equalsIgnoreCase(currentStore)) && (currentStore == null || currentStore.equalsIgnoreCase(store)))
				return;
			super.setStore(store, workingFolder);
			currentStore = store;
		}

		@Override
		public synchronized String get(String key) {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (container == null)
				return null;

			return container.optString(key, null);
		}

		@Override
		protected synchronized String getSpecific(String key, String specific) {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (container == null)
				return null;

			container = container.optJSONObject(key);
			if (container == null)
				return null;

			return container.optString(specific, null);
		}

		@Override
		public synchronized void put(String key, String value) throws JSONException {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (value != null) {
				if (container == null)
					container = makeContainer(useStore);
				container.put(key, value);
			} else if (container != null) {
				container.remove(key);
				if (useStore != null && container.length() == 0)
					breakContainer(useStore);
			}
			commit();
		}

		@Override
		protected synchronized void putSpecific(String key, String specific, String value) throws JSONException {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (value != null) {
				if (container == null)
					container = makeContainer(useStore);
				JSONObject values = container.optJSONObject(key);
				if (values == null) {
					values = new JSONObject();
					container.put(key, values);
				}
				values.put(specific, value);
			} else if (container != null) {
				JSONObject values = container.optJSONObject(key);
				if (values != null) {
					values.remove(specific);
					if (values.length() == 0)
						container.remove(key);
				}
				if (useStore != null && container.length() == 0)
					breakContainer(useStore);
			}
			commit();
		}

		public synchronized JSONArray getArray(String key, String specific) {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (container == null)
				return null;

			container = container.optJSONObject(key);
			if (container == null)
				return null;

			return container.optJSONArray(specific);
		}

		public synchronized void putArray(String key, String specific, JSONArray value) throws JSONException {
			String useStore = currentStore;
			JSONObject container = getContainer(useStore);
			if (value != null) {
				if (container == null)
					container = makeContainer(useStore);
				JSONObject values = container.optJSONObject(key);
				if (values == null) {
					values = new JSONObject();
					container.put(key, values);
				}
				values.put(specific, value);
			} else if (container != null) {
				JSONObject values = container.optJSONObject(key);
				if (values != null) {
					values.remove(specific);
					if (values.length() == 0)
						container.remove(key);
				}
				if (useStore != null && container.length() == 0)
					breakContainer(useStore);
			}
			commit();
		}

		private static JsonFilePersister create(String settingsFile, boolean prettyFile) {
			try {
				Path p = Paths.get(JsonFilePersister.class.getResource(settingsFile).getPath()).toAbsolutePath();
				Files.createDirectories(p.getParent());
				System.out.println("Reading JSON : path -> "+ JsonFilePersister.class.getResource(settingsFile).getPath());
				JSONObject json = null;
				if (Files.exists(p)) {

					//json = new JSONObject(IoAndEncodingUtil.readTextFile(p)); //TODO: Implement
				} else {
					Files.createFile(p);
					json = new JSONObject();
				}
				if (!Files.isWritable(p)) {
					System.err.println("Non-writable settings file");
					return new JsonFilePersister(new JSONObject(), null, prettyFile);
				}
				return new JsonFilePersister(json, p, prettyFile);
			} catch (IOException e) {
				//Model.onError(null, e, "Failed to load settings file");
				return new JsonFilePersister(new JSONObject(), null, prettyFile);
			}
		}

		public static JsonFilePersister create(String settingsFile) {
			return create(settingsFile, true);
		}
	}

	private JsonFilePersister metaDb;

	public void setStore(String store, Path workingFolder) {
		if (workingFolder == null)
			metaDb = null;
		else
			metaDb = JsonFilePersister.create(workingFolder.resolve("db.json").toString(), false);
	}

	public abstract String get(String key);

	protected abstract String getSpecific(String key, String specific);

	public abstract void put(String key, String value) throws JSONException;

	protected abstract void putSpecific(String key, String specific, String value) throws JSONException;

	public String getFromDb(String key, String specific) {
		return metaDb.getSpecific(key, specific);
	}

	public JSONArray getArrayFromDb(String key, String specific) {
		return metaDb.getArray(key, specific);
	}

	public void putInDb(String key, String specific, String value) throws JSONException {
		metaDb.putSpecific(key, specific, value);
	}

	public void putArrayInDb(String string, String typeName, JSONArray jsonArray) throws JSONException {
		metaDb.putArray(string, typeName, jsonArray);
	}
}
