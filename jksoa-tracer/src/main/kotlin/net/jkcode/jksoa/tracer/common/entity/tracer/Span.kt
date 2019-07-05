package net.jkcode.jksoa.tracer.common.entity.tracer

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jkmvc.common.getLocalHostPort
import net.jkcode.jkmvc.orm.OrmEntity

/**
 * span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
open class Span: OrmEntity() {

	// 代理属性读写
	public var id:Long by property() //

	public var serviceId:Int by property() // 服务类名

	public var name:String by property() // 方法名

	public var traceId:Long by property() //

	public var parentId:Long? by property() //

	public open val annotations: List<Annotation> by listProperty() //

	/**
	 * 是否发起人
	 */
	public val isInitiator: Boolean
		get(){
			// 以开始为准, 可能有异常
			return (parentId == null || parentId == 0L) && isClient
		}

	/**
	 * 是否服务端
	 */
	public val isServer: Boolean
		get(){
			// 以开始为准, 可能有异常
			return srAnnotation != null && csAnnotation == null
		}

	/**
	 * 是否客户端
	 */
	public val isClient: Boolean
		get(){
			// 以开始为准, 可能有异常
			return csAnnotation != null && srAnnotation == null
		}

	public val csAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isCs
			}
		}

	public val crAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isCr
			}
		}

	public val ssAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isSs
			}
		}

	public val srAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isSr
			}
		}

	public val exAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isEx
			}
		}

	/**
	 * 不够annotation, 则不可用
	 */
	public val isAvailable: Boolean
		get() {
			if(isInitiator)
				return annotations.size == 2

			return annotations.size == 4
			return true
		}

	/**
	 * 添加标注
	 */
	public fun addAnnotation(annotation: Annotation){
		(annotations as MutableList).add(annotation)
	}

	/**
	 * 添加标注
	 */
	public fun addAnnotation(key: String, value: String = ""){
		val annotation: Annotation = Annotation()
		annotation.key = key
		annotation.value = value
		annotation.timestamp = currMillis()
		val (ip, port) = getLocalHostPort()
		annotation.ip = ip
		annotation.port = port
		annotation.traceId = traceId
		annotation.spanId = id
		annotation.serviceId = serviceId
		addAnnotation(annotation)
	}

	/**
	 * 添加cs annotation
	 */
	public fun addCsAnnotation() {
		addAnnotation(Annotation.CLIENT_SEND)
	}

	/**
	 * 添加cr annotation
	 */
	public fun addCrAnnotation() {
		addAnnotation(Annotation.CLIENT_RECEIVE)
	}

	/**
	 * 添加sr annotation
	 */
	public fun addSrAnnotation() {
		addAnnotation(Annotation.SERVER_RECEIVE)
	}

	/**
	 * 添加 ss annotation
	 */
	public fun addSsAnnotation() {
		addAnnotation(Annotation.SERVER_SEND)
	}

	/**
	 * 记录异常 annotation
	 */
	public fun addExAnnotation(e: Throwable) {
		addAnnotation(Annotation.EXCEPTION, e.message!!)
	}

	/**
	 * 获得client的开始时间
	 */
	val startTimeClient: Long
		get() = csAnnotation.timestamp

	/**
	 * 计算client端耗时
	 */
	public fun calculateDurationClient(): Long {
		return crAnnotation.timestamp - csAnnotation.timestamp
	}

	/**
	 * 计算server端耗时
	 */
	public fun calculateDurationServer(): Long {
		return ssAnnotation.timestamp - srAnnotation.timestamp
	}

}