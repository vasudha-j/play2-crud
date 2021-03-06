package play.utils.dyn;

import static play.libs.Json.toJson;

import java.util.Map;
import java.util.Set;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import play.utils.crud.APIController;
import play.utils.meta.FieldMetadata;
import play.utils.meta.ModelMetadata;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Dynamic
public class DynamicRestController extends APIController {

	private static ALogger log = Logger.of(DynamicRestController.class);

	private ModelMetadata model;

	public DynamicRestController(ModelMetadata model) {
		super(new DynamicDAO(model), model.getKeyField().getType(), model.getType());
		this.model = model;
	}

	@Override
	public Result create() {
		Result check = checkRequired("name");
		if (check != null) {
			if (log.isDebugEnabled())
				log.debug("check : " + check);
			return check;
		}

		Object key = null;
		try {
			Object m = modelClass.newInstance();
			Map<String, FieldMetadata> fields = model.getFields();
			Set<String> fieldNames = fields.keySet();
			for (String fieldName : fieldNames) {
				String valueStr = jsonText(fieldName);
				model.setField(m, fieldName, valueStr);
			}

			key = dao.create(m);
			if (log.isDebugEnabled())
				log.debug("key : " + key);
		} catch (Exception e) {
			log.error("exception occured", e);
		}

		if (key != null)
			return created(toJson(ImmutableMap.of("status", "OK", "key", key)));
		else
			return internalServerError();
	}

}
