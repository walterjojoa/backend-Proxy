package com.taller.proxy.dto;

import com.taller.proxy.model.Plan;

/**
 * Body of POST /api/quota/upgrade. If the plan is omitted, the user is upgraded to PRO.
 */
public record UpgradeRequest(Plan plan) {
}
