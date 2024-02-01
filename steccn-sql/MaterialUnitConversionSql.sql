

/**
  sys_dict表中 对应的单位 id 1551473814357700608 m²
  sys_dict表中 对应的单位 id 1551473817008500736 吨
 */

-- 面积 单位转换的更新
# UPDATE mes_eqp_spare_unit_conversion ssuc
#     JOIN mes_eqp_spare AS ses ON ssuc.spare_id = ses.id
# SET ssuc.conversion_quantity =
#         (ses.length_quantity * ses.wide_quantity / 1000000 ),
#     ssuc.conversion_unit_id = '1551473814357700608'
# WHERE ssuc.spare_id = ses.ID and ses.name LIKE '%光伏玻璃%' and ssuc.conversion_unit_id = '1551473814357700608';
-- 重量 单位转换的更新
# UPDATE mes_eqp_spare_unit_conversion ssuc
#     JOIN mes_eqp_spare AS ses ON ssuc.spare_id = ses.id
# SET ssuc.conversion_quantity =
#         (ses.length_quantity* ses.wide_quantity * ses.thick_quantity /1000000000 * 2.5 ),
#     ssuc.conversion_unit_id = '1551473817008500736'
# WHERE ssuc.spare_id = ses.ID and ses.name LIKE '%光伏玻璃%' and ssuc.conversion_unit_id = '1551473817008500736';

-- 面积 单位转换的新增
# INSERT INTO mes_eqp_spare_unit_conversion ( spare_id, quantity, unit_id, conversion_quantity, conversion_unit_id, `status`, CREATE_BY, create_date, update_date )
# SELECT
# ses.ID,
# 1,
# ses.unit_id, ses.length_quantity * ses.wide_quantity / 1000000,
# 1551473814357700608,
# 0,
# 'system',
# CURRENT_TIMESTAMP,
# CURRENT_TIMESTAMP
# FROM
#     mes_eqp_spare AS ses
# WHERE
#     NOT EXISTS ( SELECT 1 FROM mes_eqp_spare_unit_conversion AS ssuc WHERE ssuc.spare_id = ses.ID AND ssuc.conversion_unit_id = '1551473814357700608' )
#   AND ses.NAME LIKE '%光伏玻璃%'
#   AND ses.`STATUS` = 0;

-- 重量 单位转换的新增
# INSERT INTO mes_eqp_spare_unit_conversion ( spare_id, quantity, unit_id, conversion_quantity, conversion_unit_id, `status`, CREATE_BY, create_date, update_date )
# SELECT
#     ses.ID,
#     1,
#     ses.unit_id,
#     ses.length_quantity* ses.wide_quantity * ses.thick_quantity /1000000000 * 2.5 ,
#     1551473817008500736,
#     0,
#     'system',
#     CURRENT_TIMESTAMP,
#     CURRENT_TIMESTAMP
# FROM
#     mes_eqp_spare AS ses
# WHERE
#     NOT EXISTS ( SELECT 1 FROM mes_eqp_spare_unit_conversion AS ssuc WHERE ssuc.spare_id = ses.ID AND ssuc.conversion_unit_id = '1551473817008500736' )
#   AND ses.NAME LIKE '%光伏玻璃%'
#   AND ses.`STATUS` = 0;