package io.littlehorse.common.model.getable.global.wfspec.node;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.ArrayModel;
import io.littlehorse.common.model.getable.core.variable.MapModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Comparer {

    public static int compare(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        try {
            if (left.getVal() == null && right.getVal() != null) return -1;
            if (right.getVal() == null && left.getVal() != null) return 1;
            if (right.getVal() == null && left.getVal() == null) return 0;

            if (left.getValueType() == ValueCase.MAP && right.getValueType() == ValueCase.MAP) {
                return mapsEqual(left.getMap(), right.getMap()) ? 0 : 1;
            }

            @SuppressWarnings("all")
            int result = ((Comparable) left.getVal()).compareTo((Comparable) right.getVal());

            return result;
        } catch (Exception exn) {
            throw new LHVarSubError(exn, "Failed comparing the provided values.");
        }
    }

    private static boolean mapsEqual(MapModel left, MapModel right) throws LHVarSubError {
        List<MapModel.MapEntryModel> leftEntries = left.getEntries();
        List<MapModel.MapEntryModel> rightEntries = right.getEntries();
        if (leftEntries.size() != rightEntries.size()) return false;

        Map<VariableValueModel, VariableValueModel> rightValuesByKey = new HashMap<>();
        for (MapModel.MapEntryModel rightEntry : rightEntries) {
            if (rightValuesByKey.containsKey(rightEntry.getKey())) return false;
            rightValuesByKey.put(rightEntry.getKey(), rightEntry.getValue());
        }

        for (MapModel.MapEntryModel leftEntry : leftEntries) {
            VariableValueModel rightValue = rightValuesByKey.remove(leftEntry.getKey());
            if (rightValue == null || compare(leftEntry.getValue(), rightValue) != 0) return false;
        }
        return true;
    }

    public static boolean contains(VariableValueModel left, VariableValueModel right) throws LHVarSubError {
        try {
            // Can only do for Str, Arr, and Obj
            if (left.getValueType() == ValueCase.STR) {
                String rStr = right.asStr().getStrVal();

                return left.asStr().getStrVal().contains(rStr);
            } else if (left.getValueType() == ValueCase.JSON_ARR) {
                Object rObj = right.getVal();
                List<Object> lhs = left.asArr().getJsonArrVal();

                for (Object o : lhs) {
                    if (LHUtil.deepEquals(o, rObj)) {
                        return true;
                    }
                }
                return false;
            } else if (left.getValueType() == ValueCase.JSON_OBJ) {
                return left.asObj().getJsonObjVal().containsKey(right.asStr().getStrVal());
            } else if (left.getValueType() == ValueCase.ARRAY) {
                ArrayModel leftArray = left.getArray();
                for (VariableValueModel item : leftArray.getItems()) {
                    if (item.equals(right)) return true;
                }
                return false;
            } else if (left.getValueType() == ValueCase.MAP) {
                MapModel leftMap = left.getMap();
                for (MapModel.MapEntryModel entry : leftMap.getEntries()) {
                    if (entry.getKey().equals(right)) return true;
                }
                return false;
            } else {
                throw new LHVarSubError(null, "Can't do CONTAINS on " + left.getValueType());
            }
        } catch (Exception ex) {
            log.error("Error while evaluating CONTAINS with left={} right={}", left, right, ex);
            throw new LHVarSubError(ex, "Failed evaluating CONTAINS");
        }
    }
}
