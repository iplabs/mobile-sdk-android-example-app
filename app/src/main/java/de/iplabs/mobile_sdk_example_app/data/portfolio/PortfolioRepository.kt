package de.iplabs.mobile_sdk_example_app.data.portfolio

import de.iplabs.mobile_sdk.portfolio.Portfolio

object PortfolioRepository {
	private lateinit var dao: PortfolioDao

	operator fun invoke(dao: PortfolioDao): PortfolioRepository {
		this.dao = dao

		return this
	}

	suspend fun get(): Portfolio? {
		return dao.obtain()
	}
}
