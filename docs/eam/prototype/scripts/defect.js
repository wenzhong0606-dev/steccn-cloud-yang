/* 旧原型入口兼容；旧报告仅映射发现，不迁成已验证问题。 */
(()=>{const params=new URLSearchParams(location.search),id=params.get('report');Eam.go('abnormal-events',id?{id:'LEG-'+id}:{create:'1'});})();
