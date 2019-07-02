package net.jkcode.jksoa.tracer.common.entity

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jkmvc.common.getLocalHostPort
import net.jkcode.jkmvc.orm.OrmEntity
import java.util.*

/**
 * span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
open class Span: OrmEntity() {

	// 代理属性读写
	public var id:Long by property() //

	public var service:String by property() // 服务类名

	public var name:String by property() // 方法名

	public var traceId:Long by property() //

	public var parentId:Long? by property() //

	public open val annotations: List<Annotation> = LinkedList()

	public val isTopAnntation: Boolean
		get(){
			return annotations.any {
				it.isIs
			}
		}

	public val isAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isIs
			}
		}

	public val ieAnnotation: Annotation?
		get(){
			return annotations.firstOrNull {
				it.isIe
			}
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

	public val isRoot: Boolean
		get() = parentId == null || parentId == 0L

	/**
	 * 如果某个span没有收集全4个annotation，则判定为不可用
	 */
	public val isAvailable: Boolean
		get() {
			if(isTopAnntation)
				return annotations.size == 2

			return annotations.size == 4
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
		annotation.service = service
		addAnnotation(annotation)
	}

	/**
	 * 添加is annotation
	 */
	public fun addIsAnnotation() {
		addAnnotation(Annotation.INITIATOR_START)
	}

	/**
	 * 添加ie annotation
	 */
	public fun addIeAnnotation() {
		addAnnotation(Annotation.INITIATOR_END)
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
		get(){
			return if(isRoot && isTopAnntation)
						isAnnotation.timestamp
					else
						csAnnotation.timestamp
		}

	/**
	 * 计算client端耗时
	 */
	public fun calculateDurationClient(): Long {
		if(isRoot && isTopAnntation)
			return ieAnnotation.timestamp - isAnnotation.timestamp

		return crAnnotation.timestamp - csAnnotation.timestamp
	}

	/**
	 * 计算server端耗时
	 */
	public fun calculateDurationServer(): Long {
		return ssAnnotation.timestamp - srAnnotation.timestamp
	}

}